package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepository {

    override fun observeNotifications(): Flow<List<NotificationModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = userDocument(uid).collection("notifications")
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load notifications"))
                    else -> try {
                        trySend(snapshot.documents.map { document ->
                            NotificationModel(
                                id = document.id,
                                userId = uid,
                                title = document.getString("title").orEmpty(),
                                body = document.getString("body").orEmpty(),
                                category = NotificationCategory.entries.firstOrNull {
                                    it.name == document.getString("category")
                                } ?: NotificationCategory.SYSTEM,
                                timestamp = document.getString("timestamp").orEmpty(),
                                isRead = document.getBoolean("isRead") ?: false,
                                deepLink = document.getString("deepLink"),
                                priority = document.getString("priority") ?: "NORMAL",
                                metadata = (document.get("metadata") as? Map<*, *>)
                                    ?.mapNotNull { (key, value) ->
                                        if (key is String && value is String) key to value else null
                                    }?.toMap().orEmpty()
                            )
                        })
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeUnreadCount(): Flow<Int> = observeNotifications().map { list ->
        list.count { !it.isRead }
    }

    override fun observePreferences(): Flow<NotificationPreferences> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(NotificationPreferences())
            close()
            return@callbackFlow
        }
        val registration = userDocument(uid).collection("settings").document("notifications")
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load notification preferences"))
                    !snapshot.exists() -> trySend(NotificationPreferences())
                    else -> trySend(
                        NotificationPreferences(
                            allowTransactions = snapshot.getBoolean("allowTransactions") ?: true,
                            allowTrading = snapshot.getBoolean("allowTrading") ?: true,
                            allowTradingBot = snapshot.getBoolean("allowTradingBot") ?: true,
                            allowSecurity = snapshot.getBoolean("allowSecurity") ?: true,
                            allowSystem = snapshot.getBoolean("allowSystem") ?: true,
                            allowDemoGames = snapshot.getBoolean("allowDemoGames") ?: false
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun markAsRead(id: String) {
        notificationDocument(id).update("isRead", true).await()
    }

    override suspend fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: error("Sign in to update notifications")
        val snapshot = userDocument(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .await()
        if (snapshot.isEmpty) return
        firestore.runBatch { batch ->
            snapshot.documents.forEach { batch.update(it.reference, "isRead", true) }
        }.await()
    }

    override suspend fun deleteNotification(id: String) {
        notificationDocument(id).delete().await()
    }

    override suspend fun updatePreferences(preferences: NotificationPreferences) {
        val uid = auth.currentUser?.uid ?: error("Sign in to save notification preferences")
        userDocument(uid).collection("settings").document("notifications").set(
            mapOf(
                "allowTransactions" to preferences.allowTransactions,
                "allowTrading" to preferences.allowTrading,
                "allowTradingBot" to preferences.allowTradingBot,
                "allowSecurity" to preferences.allowSecurity,
                "allowSystem" to preferences.allowSystem,
                "allowDemoGames" to preferences.allowDemoGames
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun saveNotification(notification: NotificationModel) {
        val uid = auth.currentUser?.uid ?: error("Sign in to save notifications")
        require(notification.id.isNotBlank()) { "Notification ID is required" }
        userDocument(uid).collection("notifications").document(notification.id).set(
            mapOf(
                "userId" to uid,
                "title" to notification.title,
                "body" to notification.body,
                "category" to notification.category.name,
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead,
                "deepLink" to notification.deepLink,
                "priority" to notification.priority,
                "metadata" to notification.metadata,
                "createdAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    private fun notificationDocument(id: String) =
        userDocument(auth.currentUser?.uid ?: error("Sign in to access notifications"))
            .collection("notifications")
            .document(id)

    private fun userDocument(uid: String) = firestore.collection("users").document(uid)
}
