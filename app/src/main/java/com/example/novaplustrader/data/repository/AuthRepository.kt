package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.auth.UserProfileModel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<UserProfileModel?>
    suspend fun getCurrentUser(): UserProfileModel?
    suspend fun login(email: String, password: String): Result<UserProfileModel>
    suspend fun register(fullName: String, email: String, password: String): Result<UserProfileModel>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfileModel>
    suspend fun logout(): Result<Unit>
}
