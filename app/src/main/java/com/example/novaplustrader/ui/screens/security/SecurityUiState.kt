package com.example.novaplustrader.ui.screens.security

import com.example.novaplustrader.ui.screens.security.models.ActiveSessionModel
import com.example.novaplustrader.ui.screens.security.models.SecurityActivityModel

data class SecurityUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isBiometricEnabled: Boolean = true,
    val activeSessions: List<ActiveSessionModel> = emptyList(),
    val securityActivity: List<SecurityActivityModel> = emptyList(),
    val isSignOutOthersConfirmOpen: Boolean = false,
    val isDeleteAccountConfirmOpen: Boolean = false
)
