package com.example.novaplustrader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.novaplustrader.ui.model.Currency

@Composable
fun CurrencySwitcher(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    AccessibleDropdownSelector(
        label = "Currency",
        options = Currency.entries,
        selectedOption = selectedCurrency,
        optionLabel = { it.code },
        onOptionSelected = onCurrencySelected,
        modifier = modifier
    )
}
