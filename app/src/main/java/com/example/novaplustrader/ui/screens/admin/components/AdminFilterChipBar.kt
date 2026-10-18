package com.example.novaplustrader.ui.screens.admin.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector

@Composable
fun AdminFilterChipBar(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return
    val selected = options.firstOrNull { it.equals(selectedOption, ignoreCase = true) } ?: options.first()
    AccessibleDropdownSelector(
        label = "Filter",
        options = options,
        selectedOption = selected,
        optionLabel = { it },
        onOptionSelected = onOptionSelected,
        modifier = modifier
    )
}
