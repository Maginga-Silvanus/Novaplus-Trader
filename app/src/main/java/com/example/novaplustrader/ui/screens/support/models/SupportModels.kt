package com.example.novaplustrader.ui.screens.support.models

enum class FaqCategory(val label: String) {
    ALL("All"),
    GETTING_STARTED("Getting Started"),
    ACCOUNT("Account & Profile"),
    WALLET("Wallet"),
    DEPOSITS_WITHDRAWALS("Deposits & Withdrawals"),
    TRADING("Trading"),
    BOT("Trading Bot"),
    NOTIFICATIONS("Notifications"),
    SECURITY("Security"),
    CASINO_DEMO("Casino Demo"),
    BETTING_DEMO("Betting Demo")
}

data class FaqModel(
    val id: String,
    val category: FaqCategory,
    val question: String,
    val answer: String,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

enum class TicketCategory(val label: String) {
    ACCOUNT("Account & Profile"),
    WALLET("Wallet & Balances"),
    DEPOSIT("Deposit Issue"),
    WITHDRAWAL("Withdrawal Issue"),
    TRADING("Trading Execution"),
    BOT("Trading Bot"),
    SECURITY("Security & Access"),
    TECHNICAL_ISSUE("Technical Bug"),
    OTHER("Other Request")
}

enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_USER("Waiting for You"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class SenderType {
    USER,
    SUPPORT_AGENT,
    SYSTEM
}

data class TicketMessageModel(
    val id: String,
    val ticketId: String,
    val senderType: SenderType,
    val senderName: String,
    val messageText: String,
    val timestamp: String
)

data class SupportTicketModel(
    val id: String,
    val userId: String = "usr_current",
    val subject: String,
    val category: TicketCategory,
    val status: TicketStatus = TicketStatus.OPEN,
    val createdDate: String,
    val lastUpdated: String,
    val latestResponse: String? = null,
    val transactionId: String? = null
)

val defaultFaqList = listOf(
    FaqModel(
        id = "faq_1",
        category = FaqCategory.GETTING_STARTED,
        question = "What is NovaPlus Trader?",
        answer = "NovaPlus Trader is a premium Android-first fintech platform providing wallet management, dual USD/KES balance ledgers, binary trading interfaces, automated trading bots, and demonstration gaming features."
    ),
    FaqModel(
        id = "faq_2",
        category = FaqCategory.WALLET,
        question = "How do USD and KES balances work?",
        answer = "Your wallet maintains separate real ledger balances for USD and KES. You can switch between currency views instantly on the Home or Wallet screens. Conversions and transfers are executed through secure backend ledgers."
    ),
    FaqModel(
        id = "faq_3",
        category = FaqCategory.DEPOSITS_WITHDRAWALS,
        question = "How long do M-Pesa deposits and withdrawals take?",
        answer = "M-Pesa express deposits prompt an instant STK Push on your mobile phone and credit immediately upon network confirmation. Withdrawals require security PIN verification and process within 1-5 minutes."
    ),
    FaqModel(
        id = "faq_4",
        category = FaqCategory.TRADING,
        question = "Does binary trading involve financial risk?",
        answer = "Yes. Binary trading involves significant risk of financial loss. Past market performance or chart trends do not guarantee future returns. You should only trade with funds you can afford to lose."
    ),
    FaqModel(
        id = "faq_5",
        category = FaqCategory.BOT,
        question = "Does the Nova Trading Bot guarantee profits?",
        answer = "No. The Trading Bot executes algorithmic market strategies based on configured risk parameters. Automated trading does not eliminate market risk and does not guarantee profits."
    ),
    FaqModel(
        id = "faq_6",
        category = FaqCategory.CASINO_DEMO,
        question = "Is the Casino Spin feature real money or Demo Mode?",
        answer = "The Casino Spin feature operates strictly in Demo Mode using non-monetary Demo Credits. Demo outcomes do not represent or impact your real USD/KES wallet balances."
    ),
    FaqModel(
        id = "faq_7",
        category = FaqCategory.BETTING_DEMO,
        question = "Can I win real cash from Sports Betting in the app?",
        answer = "No. The Sportsbook module operates in Demo Mode using Demo Credits for preview purposes only. No real money wagering or cash payouts occur."
    ),
    FaqModel(
        id = "faq_8",
        category = FaqCategory.SECURITY,
        question = "How is my account secured?",
        answer = "Your account is protected with Firebase Authentication, Biometric Fingerprint/Face ID authorization, 4-digit security PIN for withdrawals, and active session monitoring."
    )
)
