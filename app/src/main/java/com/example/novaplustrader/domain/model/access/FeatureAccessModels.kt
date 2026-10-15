package com.example.novaplustrader.domain.model.access

enum class AppFeature(val displayName: String) {
    WALLET("Wallet & Accounts"),
    DEPOSIT("Deposits"),
    WITHDRAW("Withdrawals"),
    SEND_MONEY("Send & Transfer"),
    TRADING("Binary Options Trading"),
    TRADING_BOT("Nova Trading Bot"),
    BETTING("Sportsbook Demo"),
    CASINO("Casino Spin Demo"),
    KYC("Identity Verification")
}

enum class EligibilityStatus(val label: String) {
    AVAILABLE("Available"),
    KYC_REQUIRED("Verification Required"),
    KYC_REVIEW("Verification Under Review"),
    RISK_RESTRICTED("Temporarily Restricted"),
    ACCOUNT_SUSPENDED("Account Restricted"),
    AGE_VERIFICATION_REQUIRED("Age Verification Required"),
    JURISDICTION_UNAVAILABLE("Region Unavailable"),
    FEATURE_DISABLED("Feature Offline"),
    MAINTENANCE("System Maintenance"),
    LIMIT_REACHED("Limit Reached"),
    RESPONSIBLE_GAMING_RESTRICTION("Responsible Use Restriction"),
    TEMPORARILY_UNAVAILABLE("Temporarily Unavailable"),
    UNKNOWN("Unknown Status")
}

data class FeatureAccessModel(
    val feature: AppFeature,
    val status: EligibilityStatus = EligibilityStatus.AVAILABLE,
    val userMessage: String = "Feature is available for your account.",
    val actionRoute: String? = null,
    val isCached: Boolean = false,
    val lastCheckedTimestamp: String = "Just now"
) {
    val isAvailable: Boolean
        get() = status == EligibilityStatus.AVAILABLE
}

data class FeatureConfiguration(
    val feature: AppFeature,
    val enabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val allowedJurisdictions: List<String> = listOf("KE", "UG", "TZ", "US", "GLOBAL"),
    val minimumKycLevel: String = "LEVEL_1",
    val minimumAge: Int = 18,
    val updatedAt: String = "Today",
    val updatedBy: String = "System Admin"
)

fun getStandardUserMessage(status: EligibilityStatus): String {
    return when (status) {
        EligibilityStatus.AVAILABLE -> "This feature is available for your account."
        EligibilityStatus.KYC_REQUIRED -> "Complete identity verification to unlock full access to this feature."
        EligibilityStatus.KYC_REVIEW -> "Your identity verification is under compliance review. Access will be unlocked shortly."
        EligibilityStatus.RISK_RESTRICTED -> "This feature is temporarily unavailable for your account. Please contact support if you need assistance."
        EligibilityStatus.ACCOUNT_SUSPENDED -> "Your account is currently restricted. Please contact support for assistance."
        EligibilityStatus.AGE_VERIFICATION_REQUIRED -> "You must verify your age (18+) before accessing this feature."
        EligibilityStatus.JURISDICTION_UNAVAILABLE -> "This feature is currently unavailable in your geographic region."
        EligibilityStatus.FEATURE_DISABLED -> "This feature is currently offline."
        EligibilityStatus.MAINTENANCE -> "This system is temporarily undergoing scheduled maintenance. Please try again later."
        EligibilityStatus.LIMIT_REACHED -> "You have reached the maximum transaction or exposure limit for this period."
        EligibilityStatus.RESPONSIBLE_GAMING_RESTRICTION -> "Access to demo entertainment features is restricted per responsible gaming controls."
        EligibilityStatus.TEMPORARILY_UNAVAILABLE -> "This feature is temporarily unavailable. Please try again later."
        EligibilityStatus.UNKNOWN -> "Checking feature eligibility..."
    }
}

fun getStandardActionRoute(status: EligibilityStatus): String? {
    return when (status) {
        EligibilityStatus.KYC_REQUIRED -> "kyc"
        EligibilityStatus.KYC_REVIEW -> "kyc"
        EligibilityStatus.ACCOUNT_SUSPENDED, EligibilityStatus.RISK_RESTRICTED -> "contact_support"
        EligibilityStatus.AGE_VERIFICATION_REQUIRED -> "kyc"
        else -> null
    }
}
