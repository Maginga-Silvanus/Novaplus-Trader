package com.example.novaplustrader

import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.EligibilityStatus
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import com.example.novaplustrader.domain.model.access.getStandardActionRoute
import com.example.novaplustrader.domain.model.access.getStandardUserMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureAccessTest {

    @Test
    fun testEligibilityStatusUserMessages() {
        val msgKyc = getStandardUserMessage(EligibilityStatus.KYC_REQUIRED)
        assertTrue(msgKyc.contains("identity verification"))

        val msgMaint = getStandardUserMessage(EligibilityStatus.MAINTENANCE)
        assertTrue(msgMaint.contains("maintenance"))

        val msgSuspended = getStandardUserMessage(EligibilityStatus.ACCOUNT_SUSPENDED)
        assertTrue(msgSuspended.contains("restricted"))
    }

    @Test
    fun testEligibilityActionRoutes() {
        assertEquals("kyc", getStandardActionRoute(EligibilityStatus.KYC_REQUIRED))
        assertEquals("contact_support", getStandardActionRoute(EligibilityStatus.ACCOUNT_SUSPENDED))
        assertNull(getStandardActionRoute(EligibilityStatus.AVAILABLE))
    }

    @Test
    fun testFeatureAccessModelAvailability() {
        val availableModel = FeatureAccessModel(
            feature = AppFeature.TRADING,
            status = EligibilityStatus.AVAILABLE
        )
        assertTrue(availableModel.isAvailable)

        val lockedModel = FeatureAccessModel(
            feature = AppFeature.WITHDRAW,
            status = EligibilityStatus.KYC_REQUIRED
        )
        assertFalse(lockedModel.isAvailable)
    }

    @Test
    fun testFeatureConfigurationDefaults() {
        val config = FeatureConfiguration(feature = AppFeature.CASINO)
        assertTrue(config.enabled)
        assertFalse(config.maintenanceMode)
        assertEquals("System Admin", config.updatedBy)
    }
}
