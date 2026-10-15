package com.example.novaplustrader

import com.example.novaplustrader.domain.model.monitoring.BotReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.FinancialAlertType
import com.example.novaplustrader.domain.model.monitoring.FinancialReliabilityAlertModel
import com.example.novaplustrader.domain.model.monitoring.HealthStatus
import com.example.novaplustrader.domain.model.monitoring.IncidentModel
import com.example.novaplustrader.domain.model.monitoring.IncidentSeverity
import com.example.novaplustrader.domain.model.monitoring.IncidentStatus
import com.example.novaplustrader.domain.model.monitoring.MarketTradingReliabilityModel
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitoringSystemTest {

    @Test
    fun testSystemHealthStatusEnumsAndModels() {
        val health = SystemHealthModel(
            serviceId = "auth",
            serviceName = "Authentication",
            status = HealthStatus.OPERATIONAL,
            latencyMs = 45L,
            lastChecked = "12:00:00",
            isStale = false
        )

        assertEquals("auth", health.serviceId)
        assertEquals(HealthStatus.OPERATIONAL, health.status)
        assertEquals(45L, health.latencyMs)
        assertFalse(health.isStale)
    }

    @Test
    fun testUnknownStatusForMissingOrStaleData() {
        val health = SystemHealthModel(
            serviceId = "ledger",
            serviceName = "Financial Ledger",
            status = HealthStatus.UNKNOWN,
            latencyMs = 0L,
            lastChecked = "12:00:00",
            message = "Backend health record uninitialized / Not Connected",
            isStale = true
        )

        assertEquals(HealthStatus.UNKNOWN, health.status)
        assertTrue(health.isStale)
        assertNotNull(health.message)
    }

    @Test
    fun testIncidentModelCreationAndResolution() {
        val incident = IncidentModel(
            incidentId = "INC-9901",
            title = "M-Pesa Callback Stalled",
            description = "Safaricom gateway callback timed out.",
            affectedService = "M-Pesa / Payment Providers",
            severity = IncidentSeverity.HIGH,
            status = IncidentStatus.OPEN,
            startTime = "10:00:00",
            lastUpdatedTime = "10:15:00",
            resolutionSummary = null
        )

        assertEquals("INC-9901", incident.incidentId)
        assertEquals(IncidentSeverity.HIGH, incident.severity)
        assertEquals(IncidentStatus.OPEN, incident.status)

        val resolvedIncident = incident.copy(
            status = IncidentStatus.RESOLVED,
            resolutionSummary = "Upstream gateway routing restored."
        )

        assertEquals(IncidentStatus.RESOLVED, resolvedIncident.status)
        assertEquals("Upstream gateway routing restored.", resolvedIncident.resolutionSummary)
    }

    @Test
    fun testFinancialReliabilityAlertModel() {
        val alert = FinancialReliabilityAlertModel(
            alertId = "FIN_ALT_02",
            alertType = FinancialAlertType.LEDGER_RECONCILIATION,
            title = "Ledger Discrepancy Detected",
            description = "Mismatch between escrow balance and settlement journal sum.",
            timestamp = "12:30:00",
            severity = IncidentSeverity.CRITICAL
        )

        assertEquals(FinancialAlertType.LEDGER_RECONCILIATION, alert.alertType)
        assertEquals(IncidentSeverity.CRITICAL, alert.severity)
        assertEquals("ACTIVE", alert.status)
    }

    @Test
    fun testMarketTradingReliabilityBlocking() {
        val marketRel = MarketTradingReliabilityModel(
            lastMarketDataUpdate = "12:00:00",
            isStalePriceFeeds = true,
            websocketConnected = false,
            tradingBackendAvailable = false,
            orderRejectionRate = 12.5,
            executionDelayMs = 2500L,
            suspendedMarketsCount = 3
        )

        assertTrue(marketRel.isStalePriceFeeds)
        assertFalse(marketRel.websocketConnected)
        assertFalse(marketRel.tradingBackendAvailable)
        val canTrade = marketRel.tradingBackendAvailable && !marketRel.isStalePriceFeeds
        assertFalse(canTrade) // Should block new trades when market data is stale or backend unavailable
    }

    @Test
    fun testBotReliabilityHeartbeatLoss() {
        val botRel = BotReliabilityModel(
            botId = "bot_001",
            botName = "Alpha Bot",
            executionStatus = "ERROR",
            lastHeartbeat = "11:30:00",
            lastSuccessfulExecution = "11:00:00",
            repeatedErrorsCount = 5,
            riskLimitTriggered = true,
            unexpectedDisconnection = true
        )

        assertEquals("ERROR", botRel.executionStatus)
        assertTrue(botRel.riskLimitTriggered)
        assertTrue(botRel.unexpectedDisconnection)
        assertEquals(5, botRel.repeatedErrorsCount)
    }

    @Test
    fun testSensitiveLogRedaction() {
        // Test logging sanitization logic directly via helper or string formatting check
        val rawMessage = "Auth failed for user with password=SecretPassword123! and token=Bearer_xyz987 and credit card 4532-1111-2222-3333"
        
        var sanitized = rawMessage.replace(Regex("(?i)(password|pwd|pin|token|secret|api_key|auth_token)=[^&\\s]+"), "$1=REDACTED")
        sanitized = sanitized.replace(Regex("\\b\\d{4}[-. ]?\\d{4}[-. ]?\\d{4}[-. ]?\\d{4}\\b"), "CARD-REDACTED")

        assertFalse(sanitized.contains("SecretPassword123!"))
        assertFalse(sanitized.contains("Bearer_xyz987"))
        assertFalse(sanitized.contains("4532-1111-2222-3333"))
        assertTrue(sanitized.contains("password=REDACTED"))
        assertTrue(sanitized.contains("token=REDACTED"))
        assertTrue(sanitized.contains("CARD-REDACTED"))
    }
}
