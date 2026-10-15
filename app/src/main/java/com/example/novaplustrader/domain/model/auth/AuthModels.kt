package com.example.novaplustrader.domain.model.auth

data class UserProfileModel(
    val uid: String,
    val email: String,
    val fullName: String,
    val isVerified: Boolean = false,
    val createdAt: String = ""
)

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: UserProfileModel) : AuthUiState
    object Unauthenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}
