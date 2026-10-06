package com.example.novaplustrader.ui

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
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

        var isQuickMenuExpanded by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
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
            },
            containerColor = BackgroundDark
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(innerPadding)
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
                    onTradeClick = { navController.navigate(Screen.Trade.route) }
                )
            }
        }
    }
}
