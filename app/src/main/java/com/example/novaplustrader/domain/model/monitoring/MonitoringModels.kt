package com.example.novaplustrader.domain.model.monitoring

enum class SubsystemServiceId(val id: String, val displayName: String) {
    AUTHENTICATION("auth", "Authentication"),
    FIRESTORE_CONNECTIVITY("firestore", "Firestore Connectivity"),
    FINANCIAL_LEDGER("ledger", "Financial Ledger"),
    MPESA_PAYMENT_PROVIDERS("mpesa_payment", "M-Pesa / Payment Providers"),
    MARKET_DATA("market_data", "Market Data"),
    TRADING_EXECUTION("trading_execution", "Trading Execution"),
    TRADING_BOT_EXECUTION("trading_bots", "Trading Bot Execution"),
    PUSH_NOTIFICATIONS("notifications", "Push Notifications"),
    KYC_PROVIDER("kyc_provider", "KYC Provider"),
    SUPPORT_SERVICES("support", "Support Services")
}

enum class HealthStatus(val label: String) {
    OPERATIONAL("Operational"),
    DEGRADED("Degraded"),
    OUTAGE("Outage"),
    MAINTENANCE("Maintenance"),
    UNKNOWN("Unknown")
}

enum class IncidentSeverity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class IncidentStatus(val label: String) {
    OPEN("Open"),
    INVESTIGATING("Investigating"),
    IDENTIFIED("Identified"),
    MONITORING("Monitoring"),
    RESOLVED("Resolved")
}

enum class FinancialAlertType(val label: String) {
    PAYMENT_RECONCILIATION("Payment Reconciliation Alert"),
    LEDGER_RECONCILIATION("Ledger Reconciliation Alert"),
    DUPLICATE_TRANSACTION("Duplicate Transaction Detection"),
    IDEMPOTENCY_CONFLICT("Idempotency Conflict"),
    PENDING_TRANSACTION("Pending Transaction Stalled"),
    FAILED_WITHDRAWAL("Failed Withdrawal Alert"),
    DELAYED_CALLBACK("Delayed Payment Callback")
}

data class SystemHealthModel(
    val serviceId: String,
    val serviceName: String,
    val status: HealthStatus = HealthStatus.UNKNOWN,
    val latencyMs: Long = 0L,
    val lastChecked: String = "Unknown",
    val message: String? = null,
    val isStale: Boolean = false
)

data class IncidentModel(
    val incidentId: String,
    val title: String,
    val description: String,
    val affectedService: String,
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val startTime: String,
    val lastUpdatedTime: String,
    val resolutionSummary: String? = null
)

data class FinancialReliabilityAlertModel(
    val alertId: String,
    val alertType: FinancialAlertType,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: IncidentSeverity,
    val status: String = "ACTIVE"
)

data class MarketTradingReliabilityModel(
    val lastMarketDataUpdate: String = "Unknown",
    val isStalePriceFeeds: Boolean = false,
    val websocketConnected: Boolean = false,
    val tradingBackendAvailable: Boolean = false,
    val orderRejectionRate: Double = 0.0,
    val executionDelayMs: Long = 0L,
    val suspendedMarketsCount: Int = 0
)

data class BotReliabilityModel(
    val botId: String,
    val botName: String,
    val executionStatus: String,
    val lastHeartbeat: String,
    val lastSuccessfulExecution: String,
    val repeatedErrorsCount: Int = 0,
    val riskLimitTriggered: Boolean = false,
    val unexpectedDisconnection: Boolean = false
)
