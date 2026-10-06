package com.example.novaplustrader.ui.screens.bot

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.bot.models.BotActiveTrade
import com.example.novaplustrader.ui.screens.bot.models.BotActivityEvent
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.screens.bot.models.BotRiskLevel
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset

data class TradingBotUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableBalance: Double = 6324.49,
    val currency: Currency = Currency.USD,
    val botStatus: BotStatus = BotStatus.OFF,
    val configuration: BotConfiguration = BotConfiguration(),
    val performance: BotPerformance = BotPerformance(),
    val activeTrades: List<BotActiveTrade> = emptyList(),
    val activityHistory: List<BotActivityEvent> = emptyList(),
    val selectedPercentage: Int? = null,
    val validationError: String? = null,
    val isStartConfirmSheetOpen: Boolean = false,
    val isStopConfirmSheetOpen: Boolean = false,
    val isAssetPickerOpen: Boolean = false,
    val isSecurityVerificationOpen: Boolean = false,
    val sessionDurationFormatted: String = "00:00:00"
)
