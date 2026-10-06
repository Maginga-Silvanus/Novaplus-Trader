package com.example.novaplustrader.ui.model

data class MarketUiModel(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val percentageChange: Double,
    val isPositiveChange: Boolean,
    val sparklineData: List<Float> = emptyList()
)
