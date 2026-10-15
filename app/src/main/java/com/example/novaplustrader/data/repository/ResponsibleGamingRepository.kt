package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingSessionModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ResponsibleGamingRepository {
    fun observeProfile(): StateFlow<ResponsibleGamingProfile>
    fun observeActivityLogs(): Flow<List<GamingActivityModel>>

    suspend fun requestSelfExclusion(
        period: ExclusionPeriod,
        reason: String
    ): Result<SelfExclusionModel>

    suspend fun updateLimits(limits: ResponsibleGamingLimits): Result<ResponsibleGamingLimits>
    suspend fun startGamingSession(feature: String): Result<GamingSessionModel>
    suspend fun endGamingSession(sessionId: String): Result<Boolean>
}
