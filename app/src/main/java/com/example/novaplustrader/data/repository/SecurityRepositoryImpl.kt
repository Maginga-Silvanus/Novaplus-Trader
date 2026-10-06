package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.security.models.ActiveSessionModel
import com.example.novaplustrader.ui.screens.security.models.SecurityActivityModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityRepositoryImpl : SecurityRepository {

    private val _isBiometricEnabled = MutableStateFlow(true)

    private val _activeSessions = MutableStateFlow(
        listOf(
            ActiveSessionModel(
                id = "s_current",
                deviceName = "Pixel 9 Pro (This Device)",
                platform = "Android 15 • Mobile App",
                lastActive = "Active now",
                isCurrent = true
            ),
            ActiveSessionModel(
                id = "s_web_mac",
                deviceName = "MacBook Pro 16\"",
                platform = "macOS • Chrome Browser",
                lastActive = "2 hours ago",
                isCurrent = false
            ),
            ActiveSessionModel(
                id = "s_win_desktop",
                deviceName = "Windows Workstation",
                platform = "Windows 11 • Desktop App",
                lastActive = "3 days ago",
                isCurrent = false
            )
        )
    )

    private val _securityActivity = MutableStateFlow(
        listOf(
            SecurityActivityModel(
                id = "sa_1",
                title = "New Device Sign-in",
                description = "Authorized login from Pixel 9 Pro",
                timestamp = "Today, 09:15 AM",
                deviceInfo = "Nairobi, Kenya • Android 15"
            ),
            SecurityActivityModel(
                id = "sa_2",
                title = "Security PIN Verified",
                description = "Authorized withdrawal request of $50.00",
                timestamp = "Yesterday, 18:40 PM",
                deviceInfo = "Pixel 9 Pro"
            ),
            SecurityActivityModel(
                id = "sa_3",
                title = "Biometric Auth Enabled",
                description = "Fingerprint / Face ID enabled for instant sign-in",
                timestamp = "12 Oct, 11:20 AM",
                deviceInfo = "Pixel 9 Pro"
            )
        )
    )

    override fun observeActiveSessions(): StateFlow<List<ActiveSessionModel>> = _activeSessions.asStateFlow()
    override fun observeSecurityActivity(): StateFlow<List<SecurityActivityModel>> = _securityActivity.asStateFlow()
    override fun observeBiometricEnabled(): StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Boolean> {
        delay(300)
        _isBiometricEnabled.value = enabled
        return Result.success(true)
    }

    override suspend fun signOutOtherSessions(): Result<Boolean> {
        delay(700)
        _activeSessions.value = _activeSessions.value.filter { it.isCurrent }
        return Result.success(true)
    }

    override suspend fun requestAccountDeletion(): Result<Boolean> {
        delay(1000) // Backend compliance request submission
        return Result.success(true)
    }
}
