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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ResponsibleGamingRepositoryImpl(
    private val service: ResponsibleGamingService = ResponsibleGamingServiceImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ResponsibleGamingRepository {

    override fun observeProfile(): StateFlow<ResponsibleGamingProfile> = service.observeProfile("usr_9901")
    override fun observeActivityLogs(): Flow<List<GamingActivityModel>> = service.observeActivityLogs("usr_9901")

    override suspend fun requestSelfExclusion(
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel> {
        val result = service.requestSelfExclusion("usr_9901", period, reason)

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
        return service.updateLimits("usr_9901", limits)
    }

    override suspend fun startGamingSession(feature: String): Result<GamingSessionModel> {
        return service.startGamingSession("usr_9901", feature)
    }

    override suspend fun endGamingSession(sessionId: String): Result<Boolean> {
        return service.endGamingSession(sessionId)
    }
}
