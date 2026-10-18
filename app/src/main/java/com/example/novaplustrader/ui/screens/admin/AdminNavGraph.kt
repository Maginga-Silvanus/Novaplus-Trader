package com.example.novaplustrader.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.novaplustrader.ui.components.LocalPrivilegedActionsEnabled
import com.example.novaplustrader.ui.navigation.Screen
import com.example.novaplustrader.ui.screens.admin.models.AdminAccessState
import com.example.novaplustrader.ui.screens.admin.models.AdminArea
import com.example.novaplustrader.ui.screens.admin.models.canAccess
import com.example.novaplustrader.ui.screens.admin.models.isViewOnly

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
                        onNavigateToUsers = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminUsers.route) },
                        onNavigateToWallets = { if (authUiState.role.canAccess(AdminArea.OPERATIONS)) navController.navigate(Screen.AdminWallets.route) },
                        onNavigateToTransactions = { if (authUiState.role.canAccess(AdminArea.OPERATIONS)) navController.navigate(Screen.AdminTransactions.route) },
                        onNavigateToTrading = { if (authUiState.role.canAccess(AdminArea.OPERATIONS)) navController.navigate(Screen.AdminTrading.route) },
                        onNavigateToBots = { if (authUiState.role.canAccess(AdminArea.OPERATIONS)) navController.navigate(Screen.AdminBots.route) },
                        onNavigateToNotifications = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminNotifications.route) },
                        onNavigateToSupport = { if (authUiState.role.canAccess(AdminArea.SUPPORT)) navController.navigate(Screen.AdminSupport.route) },
                        onNavigateToAuditLogs = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminAuditLogs.route) },
                        onNavigateToSystemStatus = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminSystemStatus.route) },
                        onNavigateToCompliance = { if (authUiState.role.canAccess(AdminArea.COMPLIANCE)) navController.navigate(Screen.AdminCompliance.route) },
                        onNavigateToFeatureAccess = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminFeatureAccess.route) },
                        onNavigateToMonitoringDashboard = { if (authUiState.role.canAccess(AdminArea.GENERAL)) navController.navigate(Screen.AdminMonitoringDashboard.route) },
                        onSignOutClick = onExitAdminPortal
                    )
                }

                composable(Screen.AdminUsers.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminUsersScreen(
                            viewModel = usersViewModel,
                            onBackClick = { navController.popBackStack() },
                            onUserClick = { userId ->
                                if (authUiState.role.canAccess(AdminArea.GENERAL)) {
                                    navController.navigate(Screen.AdminUserDetails.createRoute(userId))
                                }
                            }
                        )
                    }
                }

                composable(
                    route = Screen.AdminUserDetails.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminUserDetailsScreen(
                            userId = userId,
                            viewModel = userDetailsViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminWallets.route) {
                    AdminAreaGate(authUiState.role, AdminArea.OPERATIONS, onExitAdminPortal) {
                        AdminWalletScreen(
                            viewModel = walletViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminTransactions.route) {
                    AdminAreaGate(authUiState.role, AdminArea.OPERATIONS, onExitAdminPortal) {
                        AdminTransactionsScreen(
                            viewModel = transactionsViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminTrading.route) {
                    AdminAreaGate(authUiState.role, AdminArea.OPERATIONS, onExitAdminPortal) {
                        AdminTradingScreen(
                            viewModel = tradingViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminBots.route) {
                    AdminAreaGate(authUiState.role, AdminArea.OPERATIONS, onExitAdminPortal) {
                        AdminBotsScreen(
                            viewModel = botsViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminNotifications.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminNotificationsScreen(
                            viewModel = notificationsViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminSupport.route) {
                    AdminAreaGate(authUiState.role, AdminArea.SUPPORT, onExitAdminPortal) {
                        AdminSupportScreen(
                            viewModel = supportViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminAuditLogs.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminAuditLogsScreen(
                            viewModel = auditLogsViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminSystemStatus.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminSystemStatusScreen(
                            viewModel = systemStatusViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminMonitoringDashboard.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
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
                }

                composable(Screen.AdminSystemHealth.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        SystemHealthScreen(
                            viewModel = systemHealthViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = Screen.AdminIncidentDetails.route,
                    arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        IncidentDetailsScreen(
                            incidentId = incidentId,
                            viewModel = incidentViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminCompliance.route) {
                    AdminAreaGate(authUiState.role, AdminArea.COMPLIANCE, onExitAdminPortal) {
                        AdminComplianceScreen(
                            onBackClick = { navController.popBackStack() },
                            onSelectReview = { userId ->
                                navController.navigate("admin_kyc_review/$userId")
                            }
                        )
                    }
                }

                composable("admin_kyc_review/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                    AdminAreaGate(authUiState.role, AdminArea.COMPLIANCE, onExitAdminPortal) {
                        KycReviewScreen(
                            userId = userId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminFeatureAccess.route) {
                    AdminAreaGate(authUiState.role, AdminArea.GENERAL, onExitAdminPortal) {
                        AdminFeatureAccessScreen(
                            viewModel = featureAccessViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.AdminResponsibleGaming.route) {
                    AdminAreaGate(authUiState.role, AdminArea.COMPLIANCE, onExitAdminPortal) {
                        AdminResponsibleGamingScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
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

@Composable
private fun AdminAreaGate(
    role: com.example.novaplustrader.ui.screens.admin.models.AdminRole,
    area: AdminArea,
    onExitAdminPortal: () -> Unit,
    content: @Composable () -> Unit
) {
    if (role.canAccess(area)) {
        CompositionLocalProvider(LocalPrivilegedActionsEnabled provides !role.isViewOnly) {
            content()
        }
    } else {
        AdminAccessDeniedScreen(
            reason = "Your ${role.label} role does not have access to this administrative area.",
            onReauthenticateClick = onExitAdminPortal,
            onReturnToAppClick = onExitAdminPortal
        )
    }
}
