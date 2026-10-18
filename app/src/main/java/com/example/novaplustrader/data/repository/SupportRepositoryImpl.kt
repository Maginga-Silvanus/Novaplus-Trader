package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.support.models.FaqModel
import com.example.novaplustrader.ui.screens.support.models.SenderType
import com.example.novaplustrader.ui.screens.support.models.SupportTicketModel
import com.example.novaplustrader.ui.screens.support.models.TicketCategory
import com.example.novaplustrader.ui.screens.support.models.TicketMessageModel
import com.example.novaplustrader.ui.screens.support.models.TicketStatus
import com.example.novaplustrader.ui.screens.support.models.defaultFaqList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class SupportRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SupportRepository {

    override fun observeFaqs(): Flow<List<FaqModel>> = flowOf(defaultFaqList)

    override fun observeTickets(): Flow<List<SupportTicketModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = ticketCollection(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load support tickets"))
                    else -> try {
                        trySend(snapshot.documents.map { it.toTicket(uid) })
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeTicketById(ticketId: String): Flow<SupportTicketModel?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = ticketCollection(uid).document(ticketId).addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot == null -> close(IllegalStateException("Unable to load support ticket"))
                !snapshot.exists() -> trySend(null)
                else -> try {
                    trySend(snapshot.toTicket(uid))
                } catch (parseError: Exception) {
                    close(parseError)
                }
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeTicketMessages(ticketId: String): Flow<List<TicketMessageModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = ticketCollection(uid).document(ticketId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load support messages"))
                    else -> try {
                        trySend(snapshot.documents.map { document ->
                            TicketMessageModel(
                                id = document.id,
                                ticketId = ticketId,
                                senderType = SenderType.valueOf(
                                    document.getString("senderType") ?: error("Message has no sender type")
                                ),
                                senderName = document.getString("senderName").orEmpty(),
                                messageText = document.getString("messageText").orEmpty(),
                                timestamp = document.getTimestamp("createdAt")?.toDate()?.toString().orEmpty()
                            )
                        })
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun submitSupportTicket(
        category: TicketCategory,
        subject: String,
        description: String,
        transactionId: String?
    ): Result<SupportTicketModel> = try {
        val user = auth.currentUser ?: error("Sign in before submitting a support ticket")
        require(subject.isNotBlank() && description.isNotBlank()) {
            "A subject and description are required"
        }
        val tickets = ticketCollection(user.uid)
        val ticketRef = tickets.document()
        val messageRef = ticketRef.collection("messages").document()
        val now = FieldValue.serverTimestamp()
        firestore.runBatch { batch ->
            batch.set(
                ticketRef,
                mapOf(
                    "userId" to user.uid,
                    "subject" to subject,
                    "category" to category.name,
                    "status" to TicketStatus.OPEN.name,
                    "latestResponse" to description,
                    "transactionId" to transactionId,
                    "createdAt" to now,
                    "updatedAt" to now
                )
            )
            batch.set(
                messageRef,
                mapOf(
                    "senderType" to SenderType.USER.name,
                    "senderName" to user.displayName.orEmpty(),
                    "messageText" to description,
                    "createdAt" to now
                )
            )
        }.await()
        Result.success(
            SupportTicketModel(
                id = ticketRef.id,
                userId = user.uid,
                subject = subject,
                category = category,
                status = TicketStatus.OPEN,
                createdDate = "",
                lastUpdated = "",
                latestResponse = description,
                transactionId = transactionId
            )
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun sendTicketReply(
        ticketId: String,
        messageText: String
    ): Result<TicketMessageModel> = try {
        val user = auth.currentUser ?: error("Sign in before replying to a support ticket")
        require(messageText.isNotBlank()) { "A reply cannot be empty" }
        val ticketRef = ticketCollection(user.uid).document(ticketId)
        val ticketSnapshot = ticketRef.get().await()
        require(ticketSnapshot.exists()) { "Support ticket was not found" }
        val messageRef = ticketRef.collection("messages").document()
        val now = FieldValue.serverTimestamp()
        firestore.runBatch { batch ->
            batch.set(
                messageRef,
                mapOf(
                    "senderType" to SenderType.USER.name,
                    "senderName" to user.displayName.orEmpty(),
                    "messageText" to messageText,
                    "createdAt" to now
                )
            )
            batch.update(
                ticketRef,
                mapOf(
                    "latestResponse" to messageText,
                    "status" to TicketStatus.OPEN.name,
                    "updatedAt" to now
                )
            )
        }.await()
        Result.success(
            TicketMessageModel(
                id = messageRef.id,
                ticketId = ticketId,
                senderType = SenderType.USER,
                senderName = user.displayName.orEmpty(),
                messageText = messageText,
                timestamp = ""
            )
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun reportProblem(
        category: String,
        description: String,
        deviceMetadata: String
    ): Result<Boolean> = submitSupportTicket(
        category = TicketCategory.TECHNICAL_ISSUE,
        subject = category.ifBlank { "Technical issue" },
        description = "$description\n\nDevice: $deviceMetadata",
        transactionId = null
    ).map { true }

    private fun ticketCollection(uid: String) =
        firestore.collection("users").document(uid).collection("supportTickets")

    private fun com.google.firebase.firestore.DocumentSnapshot.toTicket(uid: String) = SupportTicketModel(
        id = id,
        userId = uid,
        subject = getString("subject") ?: error("Support ticket is missing its subject"),
        category = TicketCategory.valueOf(getString("category") ?: error("Support ticket is missing its category")),
        status = TicketStatus.valueOf(getString("status") ?: TicketStatus.OPEN.name),
        createdDate = getTimestamp("createdAt")?.toDate()?.toString().orEmpty(),
        lastUpdated = getTimestamp("updatedAt")?.toDate()?.toString().orEmpty(),
        latestResponse = getString("latestResponse"),
        transactionId = getString("transactionId")
    )
}
