package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.ResponsibleGamingService
import com.example.novaplustrader.data.remote.ResponsibleGamingServiceImpl
import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingSessionModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ResponsibleGamingRepositoryImpl(
    private val service: ResponsibleGamingService = ResponsibleGamingServiceImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ResponsibleGamingRepository {

    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()

    override fun observeProfile(): StateFlow<ResponsibleGamingProfile> = service.observeProfile(userId)
    override fun observeActivityLogs(): Flow<List<GamingActivityModel>> = service.observeActivityLogs(userId)

    override suspend fun requestSelfExclusion(
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before requesting self-exclusion."))
        val result = service.requestSelfExclusion(uid, period, reason)

        result.onSuccess { exclusion ->
            notificationRepository.saveNotification(
                NotificationModel(
                    id = "notif_excl_${System.currentTimeMillis()}",
                    title = "Self-Exclusion Activated",
                    body = "Self-exclusion period (${exclusion.period.label}) activated. Access to demo entertainment features is locked.",
                    category = NotificationCategory.SECURITY,
                    timestamp = "Just now",
                    deepLink = "responsible_gaming"
                )
            )
        }

        return result
    }

    override suspend fun updateLimits(limits: ResponsibleGamingLimits): Result<ResponsibleGamingLimits> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before updating responsible-gaming limits."))
        return service.updateLimits(uid, limits)
    }

    override suspend fun startGamingSession(feature: String): Result<GamingSessionModel> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before starting a gaming session."))
        return service.startGamingSession(uid, feature)
    }

    override suspend fun endGamingSession(sessionId: String): Result<Boolean> {
        return service.endGamingSession(sessionId)
    }
}
