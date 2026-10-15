package com.example.novaplustrader.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AuthRepository
import com.example.novaplustrader.data.repository.AuthRepositoryImpl
import com.example.novaplustrader.domain.model.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Unauthenticated)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        _uiState.update { AuthUiState.Loading }
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.update { AuthUiState.Authenticated(user) }
            } else {
                _uiState.update { AuthUiState.Unauthenticated }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { AuthUiState.Error("Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { AuthUiState.Error("Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val result = authRepository.login(email.trim(), password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.update { AuthUiState.Authenticated(user) }
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Authentication failed. Please check credentials."
                _uiState.update { AuthUiState.Error(err) }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (fullName.isBlank()) {
            _uiState.update { AuthUiState.Error("Please enter your full name.") }
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { AuthUiState.Error("Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { AuthUiState.Error("Password must be at least 6 characters.") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { AuthUiState.Error("Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val result = authRepository.register(fullName.trim(), email.trim(), password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.update { AuthUiState.Authenticated(user) }
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Registration failed. Please try again."
                _uiState.update { AuthUiState.Error(err) }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.update { AuthUiState.Authenticated(user) }
                onSuccess()
            } else {
                val error = result.exceptionOrNull()?.localizedMessage
                    ?: "Google sign-in failed. Please try again."
                _uiState.update { AuthUiState.Error(error) }
            }
        }
    }

    fun showError(message: String) {
        _uiState.update { AuthUiState.Error(message) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState.Unauthenticated }
            onSuccess()
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.update { AuthUiState.Unauthenticated }
        }
    }
}
