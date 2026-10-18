package com.example.novaplustrader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(items.indices)
    AccessibleDropdownSelector(
        label = "Choose an option",
        options = items.indices.toList(),
        selectedOption = safeIndex,
        optionLabel = items::get,
        onOptionSelected = onIndexSelected,
        modifier = modifier
    )
}
