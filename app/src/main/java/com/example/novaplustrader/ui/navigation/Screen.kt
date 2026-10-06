package com.example.novaplustrader.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Markets : Screen("markets")
    object Activity : Screen("activity")
    object Profile : Screen("profile")

    // Quick Action & Feature Destinations
    object Send : Screen("send")
    object Receive : Screen("receive")
    object Deposit : Screen("deposit")
    object Withdraw : Screen("withdraw")
    object ScanQR : Screen("scan_qr")
    object BuyCrypto : Screen("buy_crypto")
    object Trade : Screen("trade")
    object TradingBot : Screen("trading_bot")
    object CasinoSpin : Screen("casino_spin")
    object CasinoHistory : Screen("casino_history")
    object Betting : Screen("betting")
    object BettingEventDetails : Screen("betting_event")
    object BetSlip : Screen("betting_slip")
    object MyBets : Screen("my_bets")
    object BetDetails : Screen("bet_details")
    object Notifications : Screen("notifications")
    object NotificationSettings : Screen("notification_settings")
    object TransactionHistory : Screen("transaction_history")
    object TransactionDetails : Screen("transaction_details")
    object EditProfile : Screen("edit_profile")
    object Security : Screen("security")
    object ActiveSessions : Screen("active_sessions")
    object SecurityActivity : Screen("security_activity")
    object Settings : Screen("settings")
    object PrivacySettings : Screen("privacy_settings")
    object HelpCenter : Screen("help_center")
    object ContactSupport : Screen("contact_support")
    object SupportTickets : Screen("support_tickets")
    object SupportTicketDetails : Screen("support_ticket_details")
    object LegalInformation : Screen("legal_information")
    object RiskDisclosure : Screen("risk_disclosure")

    // Admin & Operations Control Center Destinations
    object AdminPortal : Screen("admin_portal")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminUsers : Screen("admin_users")
    object AdminUserDetails : Screen("admin_user_details/{userId}") {
        fun createRoute(userId: String) = "admin_user_details/$userId"
    }
    object AdminWallets : Screen("admin_wallets")
    object AdminTransactions : Screen("admin_transactions")
    object AdminTrading : Screen("admin_trading")
    object AdminBots : Screen("admin_bots")
    object AdminNotifications : Screen("admin_notifications")
    object AdminSupport : Screen("admin_support")
    object AdminAuditLogs : Screen("admin_audit_logs")
    object AdminSystemStatus : Screen("admin_system_status")
    object AdminAccessDenied : Screen("admin_access_denied")
}
