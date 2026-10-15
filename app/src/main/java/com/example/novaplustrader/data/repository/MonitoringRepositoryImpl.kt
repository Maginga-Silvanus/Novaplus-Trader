package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.MonitoringService
import com.example.novaplustrader.data.remote.MonitoringServiceImpl
import com.example.novaplustrader.domain.model.monitoring.BotReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.FinancialReliabilityAlertModel
import com.example.novaplustrader.domain.model.monitoring.IncidentModel
import com.example.novaplustrader.domain.model.monitoring.IncidentSeverity
import com.example.novaplustrader.domain.model.monitoring.IncidentStatus
import com.example.novaplustrader.domain.model.monitoring.MarketTradingReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MonitoringRepositoryImpl(
    private val monitoringService: MonitoringService = MonitoringServiceImpl()
) : MonitoringRepository {

    override fun getSystemHealth(): Flow<List<SystemHealthModel>> = flow {
        val result = monitoringService.fetchSystemHealth()
        emit(result.getOrDefault(emptyList()))
    }

    override fun getIncidents(
        searchQuery: String,
        serviceFilter: String?,
        severityFilter: IncidentSeverity?,
        statusFilter: IncidentStatus?,
        page: Int,
        pageSize: Int
    ): Flow<List<IncidentModel>> = flow {
        val result = monitoringService.fetchIncidents()
        val all = result.getOrDefault(emptyList())

        val filtered = all.filter { inc ->
            val matchesSearch = searchQuery.isBlank() ||
                    inc.title.contains(searchQuery, ignoreCase = true) ||
                    inc.description.contains(searchQuery, ignoreCase = true) ||
                    inc.incidentId.contains(searchQuery, ignoreCase = true)

            val matchesService = serviceFilter.isNullOrBlank() || inc.affectedService.equals(serviceFilter, ignoreCase = true)
            val matchesSeverity = severityFilter == null || inc.severity == severityFilter
            val matchesStatus = statusFilter == null || inc.status == statusFilter

            matchesSearch && matchesService && matchesSeverity && matchesStatus
        }

        val fromIndex = (page - 1) * pageSize
        if (fromIndex >= filtered.size) {
            emit(emptyList())
        } else {
            val toIndex = minOf(fromIndex + pageSize, filtered.size)
            emit(filtered.subList(fromIndex, toIndex))
        }
    }

    override fun getIncidentDetails(incidentId: String): Flow<IncidentModel?> = flow {
        val result = monitoringService.fetchIncidentDetails(incidentId)
        emit(result.getOrNull())
    }

    override fun getFinancialReliabilityAlerts(): Flow<List<FinancialReliabilityAlertModel>> = flow {
        val result = monitoringService.fetchFinancialAlerts()
        emit(result.getOrDefault(emptyList()))
    }

    override fun getMarketTradingReliability(): Flow<MarketTradingReliabilityModel> = flow {
        val result = monitoringService.fetchMarketTradingReliability()
        emit(result.getOrDefault(MarketTradingReliabilityModel()))
    }

    override fun getBotReliabilityList(): Flow<List<BotReliabilityModel>> = flow {
        val result = monitoringService.fetchBotReliabilityList()
        emit(result.getOrDefault(emptyList()))
    }

    override suspend fun updateIncidentStatus(incidentId: String, newStatus: String, resolutionSummary: String?): Result<Boolean> {
        return monitoringService.updateIncidentStatus(incidentId, newStatus, resolutionSummary)
    }

    override suspend fun createIncident(incident: IncidentModel): Result<Boolean> {
        return monitoringService.createIncident(incident)
    }
}
