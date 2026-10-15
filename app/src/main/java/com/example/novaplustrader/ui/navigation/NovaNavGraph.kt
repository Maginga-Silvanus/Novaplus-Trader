package com.example.novaplustrader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novaplustrader.ui.screens.admin.AdminNavGraph
import com.example.novaplustrader.ui.screens.auth.AuthViewModel
import com.example.novaplustrader.ui.screens.auth.LoginScreen
import com.example.novaplustrader.ui.screens.auth.RegisterScreen
import com.example.novaplustrader.ui.screens.auth.SplashScreen
import com.example.novaplustrader.ui.screens.auth.WelcomeScreen
import com.example.novaplustrader.ui.screens.betting.BetDetailsScreen
import com.example.novaplustrader.ui.screens.betting.BetSlipScreen
import com.example.novaplustrader.ui.screens.betting.BettingScreen
import com.example.novaplustrader.ui.screens.betting.BettingViewModel
import com.example.novaplustrader.ui.screens.betting.EventDetailsScreen
import com.example.novaplustrader.ui.screens.betting.MyBetsScreen
import com.example.novaplustrader.ui.screens.bot.TradingBotScreen
import com.example.novaplustrader.ui.screens.bot.TradingBotViewModel
import com.example.novaplustrader.ui.screens.casino.CasinoHistoryScreen
import com.example.novaplustrader.ui.screens.casino.CasinoScreen
import com.example.novaplustrader.ui.screens.casino.CasinoViewModel
import com.example.novaplustrader.ui.screens.home.HomeScreen
import com.example.novaplustrader.ui.screens.home.HomeViewModel
import com.example.novaplustrader.ui.screens.legal.LegalInformationScreen
import com.example.novaplustrader.ui.screens.legal.RiskDisclosureScreen
import com.example.novaplustrader.ui.screens.notifications.NotificationCenterScreen
import com.example.novaplustrader.ui.screens.notifications.NotificationViewModel
import com.example.novaplustrader.ui.screens.markets.MarketsScreen
import com.example.novaplustrader.ui.screens.wallet.CryptoWalletScreen
import com.example.novaplustrader.ui.screens.placeholder.FeaturePlaceholderScreen
import com.example.novaplustrader.ui.screens.profile.EditProfileScreen
import com.example.novaplustrader.ui.screens.profile.ProfileScreen
import com.example.novaplustrader.ui.screens.profile.ProfileViewModel
import com.example.novaplustrader.ui.screens.security.ActiveSessionsScreen
import com.example.novaplustrader.ui.screens.security.SecurityActivityScreen
import com.example.novaplustrader.ui.screens.security.SecurityScreen
import com.example.novaplustrader.ui.screens.security.SecurityViewModel
import com.example.novaplustrader.ui.screens.settings.NotificationSettingsScreen
import com.example.novaplustrader.ui.screens.settings.PrivacySettingsScreen
import com.example.novaplustrader.ui.screens.settings.SettingsScreen
import com.example.novaplustrader.ui.screens.support.ContactSupportScreen
import com.example.novaplustrader.ui.screens.support.HelpCenterScreen
import com.example.novaplustrader.ui.screens.support.SupportTicketDetailsScreen
import com.example.novaplustrader.ui.screens.support.SupportTicketsScreen
import com.example.novaplustrader.ui.screens.support.SupportViewModel
import com.example.novaplustrader.ui.screens.trading.TradingScreen
import com.example.novaplustrader.ui.screens.trading.TradingViewModel
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.wallet.DepositScreen
import com.example.novaplustrader.ui.screens.wallet.QrScannerScreen
import com.example.novaplustrader.ui.screens.wallet.ReceiveMoneyScreen
import com.example.novaplustrader.ui.screens.wallet.SendMoneyScreen
import com.example.novaplustrader.ui.screens.wallet.TransactionDetailsScreen
import com.example.novaplustrader.ui.screens.wallet.TransactionHistoryScreen
import com.example.novaplustrader.ui.screens.wallet.WalletScreen
import com.example.novaplustrader.ui.screens.wallet.WalletViewModel
import com.example.novaplustrader.ui.screens.wallet.WithdrawScreen

@Composable
fun NovaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    tradingViewModel: TradingViewModel = viewModel(),
    tradingBotViewModel: TradingBotViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel(),
    casinoViewModel: CasinoViewModel = viewModel(),
    bettingViewModel: BettingViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    securityViewModel: SecurityViewModel = viewModel(),
    supportViewModel: SupportViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                onNavigateToSend = { navController.navigate(Screen.Send.route) },
                onNavigateToReceive = { navController.navigate(Screen.Receive.route) },
                onNavigateToScanQR = { navController.navigate(Screen.ScanQR.route) },
                onNavigateToActivity = { navController.navigate(Screen.TransactionHistory.route) },
                onNavigateToTrade = { navController.navigate(Screen.Trade.route) },
                onNavigateToTradingBot = { navController.navigate(Screen.TradingBot.route) },
                onNavigateToCasinoSpin = { navController.navigate(Screen.CasinoSpin.route) },
                onNavigateToBetting = { navController.navigate(Screen.Betting.route) }
            )
        }

        composable(Screen.Wallet.route) {
            WalletScreen(
                viewModel = walletViewModel,
                onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                onNavigateToSend = { navController.navigate(Screen.Send.route) },
                onNavigateToReceive = { navController.navigate(Screen.Receive.route) },
                onNavigateToHistory = { navController.navigate(Screen.TransactionHistory.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.Markets.route) {
            MarketsScreen(
                tradingViewModel = tradingViewModel,
                onNavigateToTrade = { navController.navigate(Screen.Trade.route) }
            )
        }

        composable(Screen.Activity.route) {
            TransactionHistoryScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() },
                onSelectTransaction = { navController.navigate(Screen.TransactionDetails.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacySettings.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminPortal.route) },
                onNavigateToKyc = { navController.navigate(Screen.Kyc.route) },
                onNavigateToResponsibleGaming = { navController.navigate(Screen.ResponsibleGaming.route) },
                onNavigateToTrade = { navController.navigate(Screen.Trade.route) },
                onNavigateToTradingBot = { navController.navigate(Screen.TradingBot.route) },
                onNavigateToCasinoSpin = { navController.navigate(Screen.CasinoSpin.route) },
                onNavigateToBetting = { navController.navigate(Screen.Betting.route) },
                onLoggedOut = {
                    authViewModel.logout {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.AdminPortal.route) {
            val adminNavController = rememberNavController()
            AdminNavGraph(
                navController = adminNavController,
                onExitAdminPortal = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Security.route) {
            SecurityScreen(
                viewModel = securityViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToActiveSessions = { navController.navigate(Screen.ActiveSessions.route) },
                onNavigateToSecurityActivity = { navController.navigate(Screen.SecurityActivity.route) }
            )
        }

        composable(Screen.ActiveSessions.route) {
            ActiveSessionsScreen(
                viewModel = securityViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SecurityActivity.route) {
            SecurityActivityScreen(
                viewModel = securityViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacySettings.route) },
                onNavigateToHelpCenter = { navController.navigate(Screen.HelpCenter.route) },
                onNavigateToLegal = { navController.navigate(Screen.LegalInformation.route) }
            )
        }

        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.HelpCenter.route) {
            HelpCenterScreen(
                viewModel = supportViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToContactSupport = { navController.navigate(Screen.ContactSupport.route) },
                onNavigateToTickets = { navController.navigate(Screen.SupportTickets.route) }
            )
        }

        composable(Screen.ContactSupport.route) {
            ContactSupportScreen(
                viewModel = supportViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToTickets = { navController.navigate(Screen.SupportTickets.route) }
            )
        }

        composable(Screen.SupportTickets.route) {
            SupportTicketsScreen(
                viewModel = supportViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToCreateTicket = { navController.navigate(Screen.ContactSupport.route) },
                onSelectTicketDetails = { navController.navigate(Screen.SupportTicketDetails.route) }
            )
        }

        composable(Screen.SupportTicketDetails.route) {
            SupportTicketDetailsScreen(
                viewModel = supportViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LegalInformation.route) {
            LegalInformationScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToRiskDisclosure = { navController.navigate(Screen.RiskDisclosure.route) }
            )
        }

        composable(Screen.RiskDisclosure.route) {
            RiskDisclosureScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Send.route) {
            SendMoneyScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() },
                onScanQRClick = { navController.navigate(Screen.ScanQR.route) }
            )
        }

        composable(Screen.Receive.route) {
            ReceiveMoneyScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Deposit.route) {
            DepositScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Withdraw.route) {
            WithdrawScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ScanQR.route) {
            QrScannerScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() },
                onScanComplete = {
                    if (navController.previousBackStackEntry?.destination?.route == Screen.Send.route) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Send.route) {
                            popUpTo(Screen.ScanQR.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.BuyCrypto.route) {
            CryptoWalletScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToTrade = { navController.navigate(Screen.Trade.route) }
            )
        }

        composable(Screen.Trade.route) {
            TradingScreen(
                viewModel = tradingViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TradingBot.route) {
            TradingBotScreen(
                viewModel = tradingBotViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.CasinoSpin.route) {
            CasinoScreen(
                viewModel = casinoViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.CasinoHistory.route) }
            )
        }

        composable(Screen.CasinoHistory.route) {
            CasinoHistoryScreen(
                viewModel = casinoViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Betting.route) {
            BettingScreen(
                viewModel = bettingViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToEventDetails = { navController.navigate(Screen.BettingEventDetails.route) },
                onNavigateToBetSlip = { navController.navigate(Screen.BetSlip.route) },
                onNavigateToMyBets = { navController.navigate(Screen.MyBets.route) },
                onNavigateToSearch = { navController.navigate(Screen.BettingEventDetails.route) }
            )
        }

        composable(Screen.BettingEventDetails.route) {
            EventDetailsScreen(
                viewModel = bettingViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToBetSlip = { navController.navigate(Screen.BetSlip.route) }
            )
        }

        composable(Screen.BetSlip.route) {
            BetSlipScreen(
                viewModel = bettingViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToMyBets = { navController.navigate(Screen.MyBets.route) }
            )
        }

        composable(Screen.MyBets.route) {
            MyBetsScreen(
                viewModel = bettingViewModel,
                onBackClick = { navController.popBackStack() },
                onSelectBetDetails = { navController.navigate(Screen.BetDetails.route) }
            )
        }

        composable(Screen.BetDetails.route) {
            BetDetailsScreen(
                viewModel = bettingViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationCenterScreen(
                viewModel = notificationViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToDeepLink = { route ->
                    if (!route.isNullOrBlank()) {
                        navController.navigate(route)
                    }
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                viewModel = notificationViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() },
                onSelectTransaction = { navController.navigate(Screen.TransactionDetails.route) }
            )
        }

        composable(Screen.TransactionDetails.route) {
            TransactionDetailsScreen(
                viewModel = walletViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
