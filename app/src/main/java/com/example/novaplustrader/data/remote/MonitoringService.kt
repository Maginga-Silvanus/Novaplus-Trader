package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.monitoring.BotReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.FinancialReliabilityAlertModel
import com.example.novaplustrader.domain.model.monitoring.IncidentModel
import com.example.novaplustrader.domain.model.monitoring.MarketTradingReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel

interface MonitoringService {
    suspend fun fetchSystemHealth(): Result<List<SystemHealthModel>>
    suspend fun fetchIncidents(): Result<List<IncidentModel>>
    suspend fun fetchIncidentDetails(incidentId: String): Result<IncidentModel?>
    suspend fun updateIncidentStatus(incidentId: String, newStatus: String, resolutionSummary: String?): Result<Boolean>
    suspend fun createIncident(incident: IncidentModel): Result<Boolean>

    suspend fun fetchFinancialAlerts(): Result<List<FinancialReliabilityAlertModel>>
    suspend fun fetchMarketTradingReliability(): Result<MarketTradingReliabilityModel>
    suspend fun fetchBotReliabilityList(): Result<List<BotReliabilityModel>>

    fun logStructuredEvent(
        eventType: String,
        service: String,
        severity: String,
        correlationId: String,
        sanitizedErrorCode: String,
        message: String
    )
}
