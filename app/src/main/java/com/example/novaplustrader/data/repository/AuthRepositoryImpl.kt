package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.auth.UserProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    companion object {
        private var cachedFullName: String? = null
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private fun resolveName(firebaseDisplayName: String?): String {
        return firebaseDisplayName?.takeIf { it.isNotBlank() }
            ?: cachedFullName?.takeIf { it.isNotBlank() }
            ?: "Nova Trader"
    }

    override fun observeAuthState(): Flow<UserProfileModel?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(
                    UserProfileModel(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "trader@novaplustrader.com",
                        fullName = resolveName(firebaseUser.displayName),
                        isVerified = firebaseUser.isEmailVerified,
                        createdAt = dateFormat.format(Date())
                    )
                )
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getCurrentUser(): UserProfileModel? {
        val user = firebaseAuth.currentUser ?: return null
        return UserProfileModel(
            uid = user.uid,
            email = user.email ?: "trader@novaplustrader.com",
            fullName = resolveName(user.displayName),
            isVerified = user.isEmailVerified,
            createdAt = dateFormat.format(Date())
        )
    }

    override suspend fun login(email: String, password: String): Result<UserProfileModel> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Authentication user not found.")
            Result.success(
                UserProfileModel(
                    uid = user.uid,
                    email = user.email ?: email,
                    fullName = resolveName(user.displayName),
                    isVerified = user.isEmailVerified,
                    createdAt = dateFormat.format(Date())
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(fullName: String, email: String, password: String): Result<UserProfileModel> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Registration failed.")
            
            cachedFullName = fullName

            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (_: Exception) {
                // Ignore profile update network delay/failure, cachedFullName preserves user name locally
            }

            Result.success(
                UserProfileModel(
                    uid = user.uid,
                    email = user.email ?: email,
                    fullName = fullName,
                    isVerified = user.isEmailVerified,
                    createdAt = dateFormat.format(Date())
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfileModel> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Authentication user not found.")
            if (!user.displayName.isNullOrBlank()) {
                cachedFullName = user.displayName
            }
            Result.success(
                UserProfileModel(
                    uid = user.uid,
                    email = user.email ?: throw Exception("Google account email not found."),
                    fullName = resolveName(user.displayName),
                    isVerified = user.isEmailVerified,
                    createdAt = dateFormat.format(Date())
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            cachedFullName = null
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
