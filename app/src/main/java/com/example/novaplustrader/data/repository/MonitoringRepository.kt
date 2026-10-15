package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.monitoring.BotReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.FinancialReliabilityAlertModel
import com.example.novaplustrader.domain.model.monitoring.IncidentModel
import com.example.novaplustrader.domain.model.monitoring.IncidentSeverity
import com.example.novaplustrader.domain.model.monitoring.IncidentStatus
import com.example.novaplustrader.domain.model.monitoring.MarketTradingReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun getSystemHealth(): Flow<List<SystemHealthModel>>
    fun getIncidents(
        searchQuery: String = "",
        serviceFilter: String? = null,
        severityFilter: IncidentSeverity? = null,
        statusFilter: IncidentStatus? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<IncidentModel>>
    fun getIncidentDetails(incidentId: String): Flow<IncidentModel?>
    fun getFinancialReliabilityAlerts(): Flow<List<FinancialReliabilityAlertModel>>
    fun getMarketTradingReliability(): Flow<MarketTradingReliabilityModel>
    fun getBotReliabilityList(): Flow<List<BotReliabilityModel>>

    suspend fun updateIncidentStatus(incidentId: String, newStatus: String, resolutionSummary: String?): Result<Boolean>
    suspend fun createIncident(incident: IncidentModel): Result<Boolean>
}
