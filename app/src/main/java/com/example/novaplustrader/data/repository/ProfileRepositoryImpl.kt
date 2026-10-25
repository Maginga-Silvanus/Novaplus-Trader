package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.profile.models.ProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override fun observeProfile(): Flow<ProfileModel?> = callbackFlow {
        var profileListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            profileListener?.remove()
            profileListener = null
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
            } else {
                profileListener = firestore.collection("users")
                    .document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        when {
                            error != null -> close(error)
                            snapshot == null -> close(IllegalStateException("Unable to load your profile"))
                            snapshot.exists() -> try {
                                trySend(snapshot.toProfile(user.uid, user.email.orEmpty(), user.displayName.orEmpty()))
                            } catch (parseError: Exception) {
                                close(parseError)
                            }
                            else -> trySend(user.toProfile())
                        }
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            profileListener?.remove()
        }
    }

    override suspend fun updateProfile(
        fullName: String,
        displayName: String,
        phoneNumber: String
    ): Result<Boolean> = try {
        val user = auth.currentUser ?: error("Sign in before updating your profile")
        val profileRef = firestore.collection("users").document(user.uid)
        val existing = profileRef.get().await()
        val profile = mutableMapOf<String, Any>(
            "uid" to user.uid,
            "email" to user.email.orEmpty(),
            "fullName" to fullName,
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!existing.exists()) profile["createdAt"] = FieldValue.serverTimestamp()
        profileRef.set(profile, SetOptions.merge()).await()
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
        ).await()
        Result.success(true)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun logout(): Result<Boolean> = try {
        auth.signOut()
        Result.success(true)
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun com.google.firebase.auth.FirebaseUser.toProfile() = ProfileModel(
        uid = uid,
        fullName = displayName.orEmpty(),
        displayName = displayName.orEmpty().substringBefore(" "),
        email = email.orEmpty(),
        phoneNumber = phoneNumber.orEmpty(),
        photoUrl = photoUrl?.toString(),
        accountId = "NV-$uid",
        verificationStatus = "PENDING",
        accountStatus = "ACTIVE",
        createdAt = metadata?.creationTimestamp?.let { formatDate(Date(it)) }.orEmpty()
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toProfile(
        uid: String,
        email: String,
        authName: String
    ) = ProfileModel(
        uid = uid,
        fullName = getString("fullName") ?: authName,
        displayName = getString("displayName") ?: authName.substringBefore(" "),
        email = getString("email") ?: email,
        phoneNumber = getString("phoneNumber").orEmpty(),
        photoUrl = getString("photoUrl"),
        accountId = "NV-$uid",
        verificationStatus = getString("verificationStatus") ?: "PENDING",
        accountStatus = "ACTIVE",
        createdAt = getTimestamp("createdAt")?.toDate()?.let(::formatDate).orEmpty(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.let(::formatDate).orEmpty()
    )

    private fun formatDate(date: Date): String =
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
}
