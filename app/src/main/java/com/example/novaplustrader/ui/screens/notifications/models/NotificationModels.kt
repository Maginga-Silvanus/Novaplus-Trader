package com.example.novaplustrader.ui.screens.notifications.models

enum class NotificationCategory(val label: String) {
    ALL("All"),
    WALLET("Wallet"),
    TRADING("Trading"),
    BOT("Trading Bot"),
    SECURITY("Security"),
    SYSTEM("System"),
    BETTING("Betting"),
    CASINO_DEMO("Demo Games")
}

data class NotificationModel(
    val id: String,
    val userId: String = "usr_current",
    val title: String,
    val body: String,
    val category: NotificationCategory,
    val timestamp: String,
    val isRead: Boolean = false,
    val deepLink: String? = null,
    val priority: String = "NORMAL",
    val metadata: Map<String, String> = emptyMap()
)

data class NotificationPreferences(
    val allowTransactions: Boolean = true,
    val allowTrading: Boolean = true,
    val allowTradingBot: Boolean = true,
    val allowSecurity: Boolean = true, // Critical security alerts cannot be toggled off
    val allowSystem: Boolean = true,
    val allowDemoGames: Boolean = false
)
