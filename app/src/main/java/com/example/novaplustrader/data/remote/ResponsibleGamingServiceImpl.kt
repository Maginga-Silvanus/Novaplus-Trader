package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingEligibilityStatus
import com.example.novaplustrader.domain.model.gaming.GamingSessionModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResponsibleGamingServiceImpl : ResponsibleGamingService {

    private val _profile = MutableStateFlow(ResponsibleGamingProfile())

    private val _activityLogs = MutableStateFlow<List<GamingActivityModel>>(
        listOf(
            GamingActivityModel(
                activityId = "act_gm_101",
                userId = "usr_9901",
                feature = "CASINO_SPIN",
                demoStakeMinorUnits = 5000L, // 50 Demo Credits
                demoResultMinorUnits = 10000L, // 100 Demo Credits
                timestamp = "Today, 10:15"
            ),
            GamingActivityModel(
                activityId = "act_gm_102",
                userId = "usr_9901",
                feature = "SPORTSBOOK",
                demoStakeMinorUnits = 2500L, // 25 Demo Credits
                demoResultMinorUnits = 4625L, // 46.25 Demo Credits
                timestamp = "Yesterday, 14:30"
            )
        )
    )

    override fun observeProfile(userId: String): StateFlow<ResponsibleGamingProfile> = _profile.asStateFlow()
    override fun observeActivityLogs(userId: String): Flow<List<GamingActivityModel>> = _activityLogs.asStateFlow()

    override suspend fun requestSelfExclusion(
        userId: String,
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel> {
        delay(700) // Backend self-exclusion registration & compliance lock

        val exclusion = SelfExclusionModel(
            exclusionId = "excl_${System.currentTimeMillis().toString().takeLast(6)}",
            userId = userId,
            period = period,
            startTime = getFormattedTime(),
            reason = reason,
            isActive = true
        )

        _profile.update { current ->
            current.copy(
                eligibilityStatus = GamingEligibilityStatus.SELF_EXCLUDED,
                activeExclusion = exclusion,
                activeSession = null
            )
        }

        return Result.success(exclusion)
    }

    override suspend fun updateLimits(
        userId: String,
        limits: ResponsibleGamingLimits
    ): Result<ResponsibleGamingLimits> {
        delay(500)
        _profile.update { it.copy(limits = limits) }
        return Result.success(limits)
    }

    override suspend fun startGamingSession(
        userId: String,
        feature: String
    ): Result<GamingSessionModel> {
        delay(300)
        val session = GamingSessionModel(
            sessionId = "sess_gm_${System.currentTimeMillis().toString().takeLast(6)}",
            userId = userId,
            feature = feature,
            startTime = getFormattedTime(),
            status = "ACTIVE"
        )
        _profile.update { it.copy(activeSession = session) }
        return Result.success(session)
    }

    override suspend fun endGamingSession(sessionId: String): Result<Boolean> {
        delay(200)
        _profile.update { it.copy(activeSession = null) }
        return Result.success(true)
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
