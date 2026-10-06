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
    val lastActivity: String,
    val walletStatus: String = "ACTIVE",
    val tradingStatus: String = "ENABLED",
    val botStatus: String = "INACTIVE",
    val supportStatus: String = "NO_OPEN_TICKETS",
    val securitySummary: String = "2FA Active • 0 Security Alerts"
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
    val status: String, // ACTIVE, PENDING, COMPLETED, FAILED, CANCELLED
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
    val lastActivity: String,
    val errorState: String? = null
)

enum class AdminNotificationCategory(val label: String) {
    SYSTEM("System"),
    SECURITY("Security"),
    WALLET("Wallet"),
    TRADING("Trading"),
    BOT("Bot"),
    SUPPORT("Support")
}

enum class AdminNotificationAudience(val label: String) {
    ALL_USERS("All Users"),
    ACTIVE_TRADERS("Active Traders"),
    VERIFIED_USERS("Verified Users"),
    SPECIFIC_USER("Specific User")
}

enum class AdminNotificationPriority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class AdminNotificationRequest(
    val requestId: String = "",
    val title: String,
    val message: String,
    val category: AdminNotificationCategory,
    val audience: AdminNotificationAudience,
    val targetUserId: String? = null,
    val scheduleTime: String = "IMMEDIATE",
    val priority: AdminNotificationPriority = AdminNotificationPriority.MEDIUM,
    val deepLinkRoute: String = "",
    val createdBy: String = "",
    val timestamp: String = ""
)

enum class AdminSupportTicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    PENDING_USER("Pending User"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class AdminSupportTicketPriority(val label: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class AdminSupportMessageModel(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val message: String,
    val timestamp: String,
    val isInternalNote: Boolean = false
)

data class AdminSupportTicketModel(
    val ticketId: String,
    val userId: String,
    val userName: String,
    val subject: String,
    val category: String,
    val priority: AdminSupportTicketPriority,
    val status: AdminSupportTicketStatus,
    val assignedOperator: String = "Unassigned",
    val lastUpdate: String,
    val messages: List<AdminSupportMessageModel> = emptyList()
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
