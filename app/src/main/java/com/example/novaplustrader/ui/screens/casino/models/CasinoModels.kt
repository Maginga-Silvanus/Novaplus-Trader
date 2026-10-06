package com.example.novaplustrader.ui.screens.casino.models

import androidx.compose.ui.graphics.Color
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated

enum class CasinoGameStatus {
    IDLE,
    SPINNING,
    RESULT,
    ERROR
}

data class CasinoSegment(
    val multiplier: Double,
    val label: String,
    val color: Color
)

data class CasinoRound(
    val id: String,
    val timestamp: String,
    val demoStakeCredits: Double,
    val multiplier: Double,
    val resultCredits: Double,
    val demoBalanceAfter: Double
)

val defaultSpinSegments = listOf(
    CasinoSegment(multiplier = 2.0, label = "2x", color = ElectricLime),
    CasinoSegment(multiplier = 0.0, label = "0x", color = SurfaceDarkElevated),
    CasinoSegment(multiplier = 1.5, label = "1.5x", color = FinancialGreen),
    CasinoSegment(multiplier = 3.0, label = "3x", color = ElectricLime),
    CasinoSegment(multiplier = 0.0, label = "0x", color = SurfaceDarkElevated),
    CasinoSegment(multiplier = 5.0, label = "5x", color = FinancialGreen),
    CasinoSegment(multiplier = 1.0, label = "1x", color = ElectricLime),
    CasinoSegment(multiplier = 0.0, label = "0x", color = FinancialRed)
)
