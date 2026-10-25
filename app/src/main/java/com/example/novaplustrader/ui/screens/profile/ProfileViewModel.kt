package com.example.novaplustrader.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.ProfileRepository
import com.example.novaplustrader.data.repository.ProfileRepositoryImpl
import com.example.novaplustrader.ui.screens.profile.models.ProfileModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl(),
    private val auth: com.google.firebase.auth.FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfileData()
        loadRealUser()
    }

    private fun loadRealUser() {
        val user = auth.currentUser
        if (user != null) {
            val name = user.displayName.orEmpty()
            val email = user.email.orEmpty()
            val p = ProfileModel(
                uid = user.uid,
                fullName = name,
                displayName = name.substringBefore(" "),
                email = email,
                phoneNumber = user.phoneNumber.orEmpty(),
                accountId = "NV-${user.uid}",
                verificationStatus = "PENDING"
            )
            _uiState.update {
                it.copy(
                    profile = p,
                    fullNameInput = p.fullName,
                    displayNameInput = p.displayName,
                    phoneNumberInput = p.phoneNumber
                )
            }
        }
    }

    private fun observeProfileData() {
        viewModelScope.launch {
            repository.observeProfile().collect { p ->
                if (p != null) {
                    _uiState.update {
                        it.copy(
                            profile = p,
                            fullNameInput = p.fullName,
                            displayNameInput = p.displayName,
                            phoneNumberInput = p.phoneNumber
                        )
                    }
                } else {
                    _uiState.update { it.copy(profile = null) }
                }
            }
        }
    }

    fun updateFullNameInput(name: String) {
        _uiState.update { it.copy(fullNameInput = name, validationError = null, isSaveSuccess = false) }
    }

    fun updateDisplayNameInput(name: String) {
        _uiState.update { it.copy(displayNameInput = name, validationError = null, isSaveSuccess = false) }
    }

    fun updatePhoneNumberInput(phone: String) {
        _uiState.update { it.copy(phoneNumberInput = phone, validationError = null, isSaveSuccess = false) }
    }

    fun saveProfile() {
        val fullName = _uiState.value.fullNameInput.trim()
        val displayName = _uiState.value.displayNameInput.trim()
        val phone = _uiState.value.phoneNumberInput.trim()

        if (fullName.isBlank()) {
            _uiState.update { it.copy(validationError = "Full name cannot be empty") }
            return
        }
        if (displayName.isBlank()) {
            _uiState.update { it.copy(validationError = "Display name cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, validationError = null) }

        viewModelScope.launch {
            val result = repository.updateProfile(fullName, displayName, phone)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _uiState.update { it.copy(isSaveSuccess = true) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to update profile") }
            }
        }
    }

    fun openLogoutConfirmSheet() {
        _uiState.update { it.copy(isLogoutConfirmOpen = true) }
    }

    fun closeLogoutConfirmSheet() {
        _uiState.update { it.copy(isLogoutConfirmOpen = false) }
    }

    fun confirmLogout(onLoggedOut: () -> Unit) {
        _uiState.update { it.copy(isLogoutConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            repository.logout().onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onLoggedOut()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Unable to sign out")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, isSaveSuccess = false) }
    }
}
