package com.example.novaplustrader.ui.screens.trading.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.screens.trading.models.TradeDuration
import com.example.novaplustrader.ui.theme.TextMuted

@Composable
fun TradeDurationSelector(
    selectedDuration: TradeDuration,
    onDurationSelected: (TradeDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "TRADE DURATION",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        AccessibleDropdownSelector(
            label = "Trade duration",
            options = TradeDuration.entries,
            selectedOption = selectedDuration,
            optionLabel = { it.label },
            onOptionSelected = onDurationSelected
        )
    }
}
