package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.bot.models.BotActiveTrade
import com.example.novaplustrader.ui.screens.bot.models.BotActivityEvent
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import kotlinx.coroutines.flow.StateFlow

interface TradingBotRepository {
    fun observeBotStatus(): StateFlow<BotStatus>
    fun observeBotConfiguration(): StateFlow<BotConfiguration>
    fun observeActiveTrades(): StateFlow<List<BotActiveTrade>>
    fun observeBotPerformance(): StateFlow<BotPerformance>
    fun observeBotHistory(): StateFlow<List<BotActivityEvent>>

    fun updateConfiguration(configuration: BotConfiguration)
    suspend fun startBot(securityPinVerified: Boolean): Result<Boolean>
    suspend fun stopBot(): Result<Boolean>
    suspend fun pauseBot(): Result<Boolean>
    suspend fun resumeBot(): Result<Boolean>
}
