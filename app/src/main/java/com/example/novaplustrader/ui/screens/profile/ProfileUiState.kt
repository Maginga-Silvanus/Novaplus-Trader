package com.example.novaplustrader.ui.screens.profile

import com.example.novaplustrader.ui.screens.profile.models.ProfileModel

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val profile: ProfileModel? = null,
    val fullNameInput: String = "",
    val displayNameInput: String = "",
    val phoneNumberInput: String = "",
    val validationError: String? = null,
    val isSaveSuccess: Boolean = false,
    val isLogoutConfirmOpen: Boolean = false
)
