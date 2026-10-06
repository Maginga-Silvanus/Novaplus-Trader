package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.security.models.ActiveSessionModel
import com.example.novaplustrader.ui.screens.security.models.SecurityActivityModel
import kotlinx.coroutines.flow.StateFlow

interface SecurityRepository {
    fun observeActiveSessions(): StateFlow<List<ActiveSessionModel>>
    fun observeSecurityActivity(): StateFlow<List<SecurityActivityModel>>
    fun observeBiometricEnabled(): StateFlow<Boolean>

    suspend fun setBiometricEnabled(enabled: Boolean): Result<Boolean>
    suspend fun signOutOtherSessions(): Result<Boolean>
    suspend fun requestAccountDeletion(): Result<Boolean>
}
