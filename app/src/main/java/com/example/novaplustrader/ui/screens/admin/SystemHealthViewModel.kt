package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.MonitoringRepository
import com.example.novaplustrader.data.repository.MonitoringRepositoryImpl
import com.example.novaplustrader.domain.model.monitoring.BotReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.FinancialReliabilityAlertModel
import com.example.novaplustrader.domain.model.monitoring.MarketTradingReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemHealthUiState(
    val services: List<SystemHealthModel> = emptyList(),
    val financialAlerts: List<FinancialReliabilityAlertModel> = emptyList(),
    val marketTradingReliability: MarketTradingReliabilityModel = MarketTradingReliabilityModel(),
    val botReliabilityList: List<BotReliabilityModel> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastRefreshedTime: String = "Just now",
    val errorMessage: String? = null
)

class SystemHealthViewModel(
    private val repository: MonitoringRepository = MonitoringRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemHealthUiState())
    val uiState: StateFlow<SystemHealthUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val nowStr = timeFormat.format(Date())

                // Collect system health
                repository.getSystemHealth().collect { healthList ->
                    _uiState.update { it.copy(services = healthList) }
                }

                // Collect financial alerts
                repository.getFinancialReliabilityAlerts().collect { alerts ->
                    _uiState.update { it.copy(financialAlerts = alerts) }
                }

                // Collect market trading reliability
                repository.getMarketTradingReliability().collect { marketRel ->
                    _uiState.update { it.copy(marketTradingReliability = marketRel) }
                }

                // Collect bot reliability
                repository.getBotReliabilityList().collect { botList ->
                    _uiState.update {
                        it.copy(
                            botReliabilityList = botList,
                            isRefreshing = false,
                            lastRefreshedTime = nowStr
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = e.localizedMessage ?: "Failed to fetch production monitoring data."
                    )
                }
            }
        }
    }
}
