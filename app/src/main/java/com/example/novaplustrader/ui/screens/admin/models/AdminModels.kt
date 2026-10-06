package com.example.novaplustrader.ui.screens.admin.models

enum class AdminRole(val label: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Administrator"),
    SUPPORT("Support Desk"),
    OPERATIONS("Operations"),
    COMPLIANCE("Compliance Officer"),
    READ_ONLY("Read Only")
}

enum class AdminAccessState {
    AUTHORIZED,
    ACCESS_DENIED,
    LOADING,
    UNAUTHENTICATED
}

enum class AdminUserStatus(val label: String) {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    LOCKED("Locked"),
    PENDING("Pending Verification"),
    CLOSED("Closed")
}

data class AdminUserModel(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: AdminRole = AdminRole.READ_ONLY,
    val accountStatus: AdminUserStatus = AdminUserStatus.ACTIVE,
    val verificationStatus: String = "VERIFIED",
    val createdAt: String,
    val lastActivity: String
)

data class AdminMetricsModel(
    val totalUsers: Int = 12480,
    val activeUsers: Int = 3890,
    val newUsersToday: Int = 142,
    val suspendedUsers: Int = 18,

    val totalWallets: Int = 18920,
    val pendingDeposits: Int = 7,
    val pendingWithdrawals: Int = 12,
    val failedTransactionsToday: Int = 3,

    val activeTrades: Int = 342,
    val pendingTrades: Int = 19,
    val completedTradesToday: Int = 8940,

    val activeBots: Int = 124,
    val stoppedBots: Int = 15,
    val botErrors: Int = 2,

    val openSupportTickets: Int = 14,
    val pendingSupportRequests: Int = 5,

    val systemStatusSummary: String = "All Operational"
)

data class AdminWalletModel(
    val walletId: String,
    val userId: String,
    val userName: String,
    val currency: String,
    val availableBalance: Double,
    val pendingBalance: Double,
    val status: String = "ACTIVE",
    val lastTransaction: String,
    val riskHold: Boolean = false
)

data class AdminTransactionModel(
    val transactionId: String,
    val userId: String,
    val userName: String,
    val type: String, // DEPOSIT, WITHDRAWAL, TRANSFER, TRADE, FEE, REFUND, ADJUSTMENT
    val status: String, // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
    val amount: Double,
    val currency: String,
    val timestamp: String
)

data class AdminTradeModel(
    val tradeId: String,
    val userId: String,
    val assetPair: String,
    val direction: String,
    val amount: Double,
    val duration: String,
    val status: String,
    val timestamp: String
)

data class AdminBotModel(
    val botId: String,
    val ownerUid: String,
    val ownerName: String,
    val pair: String,
    val strategy: String,
    val riskLevel: String,
    val allocation: Double,
    val status: String, // ACTIVE, PAUSED, STOPPED, ERROR
    val startedTime: String,
    val lastActivity: String
)

data class AdminAuditLogModel(
    val auditId: String,
    val actorUid: String,
    val actorRole: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val timestamp: String,
    val result: String = "SUCCESS",
    val metadata: String = ""
)

enum class HealthStatus(val label: String) {
    OPERATIONAL("Operational"),
    DEGRADED("Degraded"),
    UNAVAILABLE("Unavailable"),
    UNKNOWN("Unknown")
}

data class AdminSystemHealthModel(
    val serviceName: String,
    val status: HealthStatus,
    val latencyMs: Int,
    val lastChecked: String
)
