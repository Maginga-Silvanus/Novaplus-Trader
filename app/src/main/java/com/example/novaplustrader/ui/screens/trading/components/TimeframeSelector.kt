package com.example.novaplustrader.ui.screens.trading.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.screens.trading.models.Timeframe

@Composable
fun TimeframeSelector(
    selectedTimeframe: Timeframe,
    onTimeframeSelected: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    AccessibleDropdownSelector(
        label = "Chart timeframe",
        options = Timeframe.entries,
        selectedOption = selectedTimeframe,
        optionLabel = { it.label },
        onOptionSelected = onTimeframeSelected,
        modifier = modifier
    )
}
