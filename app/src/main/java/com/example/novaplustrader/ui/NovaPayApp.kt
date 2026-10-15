package com.example.novaplustrader.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.novaplustrader.ui.components.BottomNavigationBar
import com.example.novaplustrader.ui.components.ExpandableQuickActionsMenu
import com.example.novaplustrader.ui.navigation.NovaNavGraph
import com.example.novaplustrader.ui.navigation.Screen
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.NovaPlusTraderTheme

@Composable
fun NovaPayApp() {
    NovaPlusTraderTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

        val context = LocalContext.current
        BackHandler(enabled = currentRoute == Screen.Home.route || currentRoute == Screen.Welcome.route) {
            (context as? Activity)?.moveTaskToBack(true)
        }

        val routesWithoutBottomBar = setOf(
            Screen.Splash.route,
            Screen.Welcome.route,
            Screen.Login.route,
            Screen.Register.route
        )
        val showBottomBar = currentRoute !in routesWithoutBottomBar

        var isQuickMenuExpanded by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            isQuickMenuExpanded = false
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onCenterPlusClick = {
                            isQuickMenuExpanded = !isQuickMenuExpanded
                        },
                        isQuickMenuExpanded = isQuickMenuExpanded
                    )
                }
            },
            containerColor = BackgroundDark
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(innerPadding)
                    .padding(top = 8.dp)
            ) {
                NovaNavGraph(navController = navController)

                // Expanding Quick Actions Overlay Menu
                ExpandableQuickActionsMenu(
                    isVisible = isQuickMenuExpanded,
                    onDismiss = { isQuickMenuExpanded = false },
                    onDepositClick = { navController.navigate(Screen.Deposit.route) },
                    onSendClick = { navController.navigate(Screen.Send.route) },
                    onReceiveClick = { navController.navigate(Screen.Receive.route) },
                    onScanQRClick = { navController.navigate(Screen.ScanQR.route) },
                    onBuyCryptoClick = { navController.navigate(Screen.BuyCrypto.route) },
                    onTradeClick = { navController.navigate(Screen.Trade.route) },
                    onTradingBotClick = { navController.navigate(Screen.TradingBot.route) },
                    onCasinoSpinClick = { navController.navigate(Screen.CasinoSpin.route) },
                    onBettingClick = { navController.navigate(Screen.Betting.route) }
                )
            }
        }
    }
}
