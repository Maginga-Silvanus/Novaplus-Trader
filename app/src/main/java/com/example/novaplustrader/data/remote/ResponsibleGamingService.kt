package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingSessionModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ResponsibleGamingService {
    fun observeProfile(userId: String): StateFlow<ResponsibleGamingProfile>
    fun observeActivityLogs(userId: String): Flow<List<GamingActivityModel>>

    suspend fun requestSelfExclusion(
        userId: String,
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel>

    suspend fun updateLimits(
        userId: String,
        limits: ResponsibleGamingLimits
    ): Result<ResponsibleGamingLimits>

    suspend fun startGamingSession(
        userId: String,
        feature: String
    ): Result<GamingSessionModel>

    suspend fun endGamingSession(sessionId: String): Result<Boolean>
}
