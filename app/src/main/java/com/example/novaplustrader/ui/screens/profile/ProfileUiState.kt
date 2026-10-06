package com.example.novaplustrader.ui.screens.profile

import com.example.novaplustrader.ui.screens.profile.models.ProfileModel

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val profile: ProfileModel? = ProfileModel(),
    val fullNameInput: String = "Leonard Lopez",
    val displayNameInput: String = "Leonard",
    val phoneNumberInput: String = "+254 712 345 678",
    val validationError: String? = null,
    val isSaveSuccess: Boolean = false,
    val isLogoutConfirmOpen: Boolean = false
)
