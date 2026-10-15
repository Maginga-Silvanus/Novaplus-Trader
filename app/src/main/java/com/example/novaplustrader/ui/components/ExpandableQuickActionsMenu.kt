package com.example.novaplustrader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted

@Composable
fun ExpandableQuickActionsMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDepositClick: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onScanQRClick: () -> Unit,
    onBuyCryptoClick: () -> Unit,
    onTradeClick: () -> Unit,
    onTradingBotClick: () -> Unit = {},
    onCasinoSpinClick: () -> Unit = {},
    onBettingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayDark)
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = modifier
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceDarkElevated)
                        .clickable(enabled = false) {}
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "QUICK ACTIONS",
                                    color = ElectricLime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Choose an operation or mode",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            IconActionButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                onClick = onDismiss
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionButton(
                                label = "Deposit",
                                icon = Icons.Default.ArrowDownward,
                                onClick = {
                                    onDismiss()
                                    onDepositClick()
                                }
                            )
                            QuickActionButton(
                                label = "Send",
                                icon = Icons.Default.Send,
                                onClick = {
                                    onDismiss()
                                    onSendClick()
                                }
                            )
                            QuickActionButton(
                                label = "Receive",
                                icon = Icons.Default.ArrowUpward,
                                onClick = {
                                    onDismiss()
                                    onReceiveClick()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionButton(
                                label = "Trade",
                                icon = Icons.AutoMirrored.Filled.ShowChart,
                                onClick = {
                                    onDismiss()
                                    onTradeClick()
                                }
                            )
                            QuickActionButton(
                                label = "Scan QR",
                                icon = Icons.Default.QrCodeScanner,
                                onClick = {
                                    onDismiss()
                                    onScanQRClick()
                                }
                            )
                            QuickActionButton(
                                label = "Buy Crypto",
                                icon = Icons.Default.CurrencyBitcoin,
                                onClick = {
                                    onDismiss()
                                    onBuyCryptoClick()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionButton(
                                label = "Bot Engine",
                                icon = Icons.Default.Android,
                                onClick = {
                                    onDismiss()
                                    onTradingBotClick()
                                }
                            )
                            QuickActionButton(
                                label = "Casino Spin",
                                icon = Icons.Default.Casino,
                                onClick = {
                                    onDismiss()
                                    onCasinoSpinClick()
                                }
                            )
                            QuickActionButton(
                                label = "Sportsbook",
                                icon = Icons.Default.SportsSoccer,
                                onClick = {
                                    onDismiss()
                                    onBettingClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
