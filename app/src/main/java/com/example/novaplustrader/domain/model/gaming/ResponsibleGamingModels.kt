package com.example.novaplustrader.domain.model.gaming

enum class GamingEligibilityStatus(val label: String) {
    NORMAL("Normal Access"),
    LIMITED("Activity Limited"),
    SELF_EXCLUDED("Self Excluded"),
    TEMPORARILY_BLOCKED("Temporarily Blocked"),
    PERMANENTLY_BLOCKED("Permanently Excluded")
}

enum class ExclusionPeriod(val label: String, val durationDays: Int) {
    TWENTY_FOUR_HOURS("24 Hours", 1),
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30),
    NINETY_DAYS("90 Days", 90),
    PERMANENT("Permanent Self-Exclusion", 36500)
}

data class ResponsibleGamingLimits(
    val dailyDemoLimitMinorUnits: Long = 500000L,     // 5,000 Demo Credits
    val weeklyDemoLimitMinorUnits: Long = 2500000L,   // 25,000 Demo Credits
    val monthlyDemoLimitMinorUnits: Long = 10000000L, // 100,000 Demo Credits
    val maxSessionDurationMinutes: Int = 120,
    val realityCheckIntervalMinutes: Int = 30
) {
    val dailyDemoLimitDouble: Double get() = dailyDemoLimitMinorUnits / 100.0
    val weeklyDemoLimitDouble: Double get() = weeklyDemoLimitMinorUnits / 100.0
    val monthlyDemoLimitDouble: Double get() = monthlyDemoLimitMinorUnits / 100.0
}

data class SelfExclusionModel(
    val exclusionId: String,
    val userId: String = "usr_9901",
    val period: ExclusionPeriod,
    val startTime: String,
    val endTime: String? = null,
    val reason: String = "Personal Choice",
    val isActive: Boolean = true
)

data class GamingSessionModel(
    val sessionId: String,
    val userId: String = "usr_9901",
    val feature: String, // "CASINO" or "BETTING"
    val startTime: String,
    val endTime: String? = null,
    val durationMinutes: Int = 0,
    val status: String = "ACTIVE"
)

data class GamingActivityModel(
    val activityId: String,
    val userId: String = "usr_9901",
    val feature: String,
    val demoStakeMinorUnits: Long,
    val demoResultMinorUnits: Long,
    val timestamp: String,
    val status: String = "COMPLETED"
) {
    val demoStakeDouble: Double get() = demoStakeMinorUnits / 100.0
    val demoResultDouble: Double get() = demoResultMinorUnits / 100.0
}

data class ResponsibleGamingProfile(
    val userId: String = "usr_9901",
    val eligibilityStatus: GamingEligibilityStatus = GamingEligibilityStatus.NORMAL,
    val limits: ResponsibleGamingLimits = ResponsibleGamingLimits(),
    val activeExclusion: SelfExclusionModel? = null,
    val activeSession: GamingSessionModel? = null,
    val ageVerified: Boolean = true,
    val jurisdictionAllowed: Boolean = true
) {
    val isEligible: Boolean
        get() = eligibilityStatus == GamingEligibilityStatus.NORMAL &&
                activeExclusion == null &&
                ageVerified &&
                jurisdictionAllowed
}
