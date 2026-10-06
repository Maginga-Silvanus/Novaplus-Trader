package com.example.novaplustrader.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class NovaHaptics(private val hapticFeedback: HapticFeedback) {
    fun lightTap() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun selection() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun confirmation() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberNovaHaptics(): NovaHaptics {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) {
        NovaHaptics(hapticFeedback)
    }
}
