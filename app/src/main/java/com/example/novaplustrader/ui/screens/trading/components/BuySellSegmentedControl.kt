package com.example.novaplustrader.ui.screens.trading.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection

@Composable
fun BuySellSegmentedControl(
    selectedDirection: TradeDirection,
    onDirectionSelected: (TradeDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    AccessibleDropdownSelector(
        label = "Trade side",
        options = TradeDirection.entries,
        selectedOption = selectedDirection,
        optionLabel = { it.label },
        onOptionSelected = onDirectionSelected,
        modifier = modifier
    )
}
