package com.example.novaplustrader.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.navigation.Screen
import com.example.novaplustrader.ui.screens.admin.models.AdminAccessState

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    onExitAdminPortal: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AdminAuthViewModel = viewModel(),
    dashboardViewModel: AdminDashboardViewModel = viewModel(),
    usersViewModel: AdminUsersViewModel = viewModel(),
    userDetailsViewModel: AdminUserDetailsViewModel = viewModel(),
    walletViewModel: AdminWalletViewModel = viewModel(),
    transactionsViewModel: AdminTransactionsViewModel = viewModel(),
    tradingViewModel: AdminTradingViewModel = viewModel(),
    botsViewModel: AdminBotsViewModel = viewModel(),
    notificationsViewModel: AdminNotificationsViewModel = viewModel(),
    supportViewModel: AdminSupportViewModel = viewModel(),
    auditLogsViewModel: AdminAuditLogsViewModel = viewModel(),
    systemStatusViewModel: AdminSystemStatusViewModel = viewModel(),
    featureAccessViewModel: AdminFeatureAccessViewModel = viewModel(),
    systemHealthViewModel: SystemHealthViewModel = viewModel(),
    incidentViewModel: IncidentViewModel = viewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()

    when (authUiState.accessState) {
        AdminAccessState.LOADING -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        }

        AdminAccessState.ACCESS_DENIED, AdminAccessState.UNAUTHENTICATED -> {
            AdminAccessDeniedScreen(
                reason = authUiState.errorMessage ?: "Your account lacks administrative authorization clearance.",
                onReauthenticateClick = { authViewModel.verifySession() },
                onReturnToAppClick = onExitAdminPortal,
                modifier = modifier
            )
        }

        AdminAccessState.AUTHORIZED -> {
            NavHost(
                navController = navController,
                startDestination = Screen.AdminDashboard.route,
                modifier = modifier
            ) {
                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                        onNavigateToWallets = { navController.navigate(Screen.AdminWallets.route) },
                        onNavigateToTransactions = { navController.navigate(Screen.AdminTransactions.route) },
                        onNavigateToTrading = { navController.navigate(Screen.AdminTrading.route) },
                        onNavigateToBots = { navController.navigate(Screen.AdminBots.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.AdminNotifications.route) },
                        onNavigateToSupport = { navController.navigate(Screen.AdminSupport.route) },
                        onNavigateToAuditLogs = { navController.navigate(Screen.AdminAuditLogs.route) },
                        onNavigateToSystemStatus = { navController.navigate(Screen.AdminSystemStatus.route) },
                        onNavigateToCompliance = { navController.navigate(Screen.AdminCompliance.route) },
                        onNavigateToFeatureAccess = { navController.navigate(Screen.AdminFeatureAccess.route) },
                        onNavigateToMonitoringDashboard = { navController.navigate(Screen.AdminMonitoringDashboard.route) },
                        onSignOutClick = onExitAdminPortal
                    )
                }

                composable(Screen.AdminUsers.route) {
                    AdminUsersScreen(
                        viewModel = usersViewModel,
                        onBackClick = { navController.popBackStack() },
                        onUserClick = { userId ->
                            navController.navigate(Screen.AdminUserDetails.createRoute(userId))
                        }
                    )
                }

                composable(
                    route = Screen.AdminUserDetails.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    AdminUserDetailsScreen(
                        userId = userId,
                        viewModel = userDetailsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminWallets.route) {
                    AdminWalletScreen(
                        viewModel = walletViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminTransactions.route) {
                    AdminTransactionsScreen(
                        viewModel = transactionsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminTrading.route) {
                    AdminTradingScreen(
                        viewModel = tradingViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminBots.route) {
                    AdminBotsScreen(
                        viewModel = botsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminNotifications.route) {
                    AdminNotificationsScreen(
                        viewModel = notificationsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminSupport.route) {
                    AdminSupportScreen(
                        viewModel = supportViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminAuditLogs.route) {
                    AdminAuditLogsScreen(
                        viewModel = auditLogsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminSystemStatus.route) {
                    AdminSystemStatusScreen(
                        viewModel = systemStatusViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminMonitoringDashboard.route) {
                    MonitoringDashboardScreen(
                        systemHealthViewModel = systemHealthViewModel,
                        incidentViewModel = incidentViewModel,
                        onNavigateToSystemHealth = { navController.navigate(Screen.AdminSystemHealth.route) },
                        onNavigateToIncidentDetails = { incidentId ->
                            navController.navigate(Screen.AdminIncidentDetails.createRoute(incidentId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminSystemHealth.route) {
                    SystemHealthScreen(
                        viewModel = systemHealthViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AdminIncidentDetails.route,
                    arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
                    IncidentDetailsScreen(
                        incidentId = incidentId,
                        viewModel = incidentViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminCompliance.route) {
                    AdminComplianceScreen(
                        onBackClick = { navController.popBackStack() },
                        onSelectReview = { userId ->
                            navController.navigate("admin_kyc_review/$userId")
                        }
                    )
                }

                composable("admin_kyc_review/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: "usr_9901"
                    KycReviewScreen(
                        userId = userId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminFeatureAccess.route) {
                    AdminFeatureAccessScreen(
                        viewModel = featureAccessViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminResponsibleGaming.route) {
                    AdminResponsibleGamingScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminAccessDenied.route) {
                    AdminAccessDeniedScreen(
                        onReauthenticateClick = { authViewModel.verifySession() },
                        onReturnToAppClick = onExitAdminPortal
                    )
                }
            }
        }
    }
}
