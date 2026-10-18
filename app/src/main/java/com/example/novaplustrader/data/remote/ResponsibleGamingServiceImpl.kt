package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingEligibilityStatus
import com.example.novaplustrader.domain.model.gaming.GamingSessionModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResponsibleGamingServiceImpl : ResponsibleGamingService {

    private val _profile = MutableStateFlow(ResponsibleGamingProfile())

    private val _activityLogs = MutableStateFlow<List<GamingActivityModel>>(emptyList())

    override fun observeProfile(userId: String): StateFlow<ResponsibleGamingProfile> = _profile.asStateFlow()
    override fun observeActivityLogs(userId: String): Flow<List<GamingActivityModel>> = _activityLogs.asStateFlow()

    override suspend fun requestSelfExclusion(
        userId: String,
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel> {
        return Result.failure(
            IllegalStateException("Responsible-gaming backend is not configured. Self-exclusion was not saved or activated.")
        )
    }

    override suspend fun updateLimits(
        userId: String,
        limits: ResponsibleGamingLimits
    ): Result<ResponsibleGamingLimits> {
        return Result.failure(
            IllegalStateException("Responsible-gaming backend is not configured. Limits were not saved.")
        )
    }

    override suspend fun startGamingSession(
        userId: String,
        feature: String
    ): Result<GamingSessionModel> {
        return Result.failure(
            IllegalStateException("Responsible-gaming backend is not configured. No gaming session was started.")
        )
    }

    override suspend fun endGamingSession(sessionId: String): Result<Boolean> {
        return Result.failure(
            IllegalStateException("Responsible-gaming backend is not configured. Session status was not changed.")
        )
    }
}
