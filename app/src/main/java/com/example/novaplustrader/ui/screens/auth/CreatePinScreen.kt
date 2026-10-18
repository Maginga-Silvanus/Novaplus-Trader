package com.example.novaplustrader.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeGlow
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun CreatePinScreen(
    onPinCreatedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onKeyPress(digit: String) {
        val target = if (!isConfirming) pin else confirmPin
        if (target.length < 8) {
            val updated = target + digit
            if (!isConfirming) pin = updated else confirmPin = updated
            errorMessage = null
        }
    }

    fun onBackspace() {
        val target = if (!isConfirming) pin else confirmPin
        if (target.isNotEmpty()) {
            val updated = target.dropLast(1)
            if (!isConfirming) pin = updated else confirmPin = updated
            errorMessage = null
        }
    }

    fun onConfirm() {
        if (!isConfirming) {
            if (pin.length < 4) {
                errorMessage = "PIN must be at least 4 digits"
                return
            }
            isConfirming = true
        } else {
            if (confirmPin != pin) {
                errorMessage = "PINs do not match. Try again."
                confirmPin = ""
                isConfirming = false
                pin = ""
                return
            }
            onPinCreatedSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (!isConfirming) "Create Security PIN" else "Confirm Security PIN",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (!isConfirming) "Enter 4 to 8 digits for your wallet security" else "Re-enter your PIN to confirm",
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicator Dots
            val currentString = if (!isConfirming) pin else confirmPin
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lengthToDisplay = maxOf(4, currentString.length)
                repeat(lengthToDisplay) { index ->
                    val isFilled = index < currentString.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) ElectricLime else SurfaceBorder)
                            .border(1.dp, if (isFilled) ElectricLime else SurfaceBorder, CircleShape)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = FinancialRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Keypad with mirror effect and subtle glowing shadow edges
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "⌫")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = ElectricLimeGlow.copy(alpha = 0.25f),
                                    spotColor = ElectricLimeGlow.copy(alpha = 0.25f)
                                )
                                .clip(CircleShape)
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, CircleShape)
                                .clickable {
                                    when (key) {
                                        "C" -> if (!isConfirming) pin = "" else confirmPin = ""
                                        "⌫" -> onBackspace()
                                        else -> onKeyPress(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = if (!isConfirming) "Next" else "Confirm PIN",
                onClick = { onConfirm() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
