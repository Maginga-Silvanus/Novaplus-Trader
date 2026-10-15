package com.example.novaplustrader

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.GamingEligibilityStatus
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingLimits
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile
import com.example.novaplustrader.domain.model.gaming.SelfExclusionModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponsibleGamingTest {

    @Test
    fun testExclusionPeriodLabelsAndDays() {
        assertEquals("24 Hours", ExclusionPeriod.TWENTY_FOUR_HOURS.label)
        assertEquals(1, ExclusionPeriod.TWENTY_FOUR_HOURS.durationDays)

        assertEquals("7 Days", ExclusionPeriod.SEVEN_DAYS.label)
        assertEquals(7, ExclusionPeriod.SEVEN_DAYS.durationDays)

        assertEquals("Permanent Self-Exclusion", ExclusionPeriod.PERMANENT.label)
        assertEquals(36500, ExclusionPeriod.PERMANENT.durationDays)
    }

    @Test
    fun testResponsibleGamingProfileEligibility() {
        val normalProfile = ResponsibleGamingProfile()
        assertTrue(normalProfile.isEligible)

        val selfExcludedProfile = ResponsibleGamingProfile(
            eligibilityStatus = GamingEligibilityStatus.SELF_EXCLUDED,
            activeExclusion = SelfExclusionModel(
                exclusionId = "excl_001",
                period = ExclusionPeriod.SEVEN_DAYS,
                startTime = "Today, 10:00"
            )
        )

        assertFalse(selfExcludedProfile.isEligible)
        assertNotNull(selfExcludedProfile.activeExclusion)
    }

    @Test
    fun testResponsibleGamingDemoLimitsAndPrecision() {
        val limits = ResponsibleGamingLimits(
            dailyDemoLimitMinorUnits = 500000L // 5,000 Demo Credits
        )

        assertEquals(5000.00, limits.dailyDemoLimitDouble, 0.001)

        val activity = GamingActivityModel(
            activityId = "act_001",
            feature = "CASINO_SPIN",
            demoStakeMinorUnits = 5000L,  // 50 Demo Credits
            demoResultMinorUnits = 10000L, // 100 Demo Credits
            timestamp = "10:15"
        )

        assertEquals(50.00, activity.demoStakeDouble, 0.001)
        assertEquals(100.00, activity.demoResultDouble, 0.001)
    }
}
