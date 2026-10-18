package com.example.novaplustrader.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.CryptoWalletRepository
import com.example.novaplustrader.data.repository.CryptoWalletRepositoryImpl
import com.example.novaplustrader.data.repository.AuthRepository
import com.example.novaplustrader.data.repository.AuthRepositoryImpl
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.WalletUiModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val mainWalletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl(),
    private val cryptoWalletRepository: CryptoWalletRepository = CryptoWalletRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeUnreadNotifications()
        observeAuthState()
    }

    fun claimFirstTimeReward() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            mainWalletRepository.claimFirstTimeReward(user.uid)
                .onSuccess {
                    notificationRepository.saveNotification(
                        NotificationModel(
                            id = UUID.randomUUID().toString(),
                            userId = user.uid,
                            title = "First-Time Reward Claimed!",
                            body = "You have successfully claimed your $5.00 USD welcome reward.",
                            category = NotificationCategory.WALLET,
                            timestamp = "Just now"
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Unable to claim reward") }
                }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { user ->
                if (user != null) {
                    val fullName = user.fullName.takeIf { it.isNotBlank() } ?: "Nova Trader"
                    val publicWalletId = "NV-${user.uid}"
                    val initials = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                    _uiState.update { current ->
                        current.copy(
                            userName = fullName,
                            userInitials = initials.ifEmpty { "NT" },
                            wallets = current.wallets.map { w ->
                                w.copy(userName = fullName, walletId = publicWalletId)
                            }
                        )
                    }
                    cryptoWalletRepository.ensureWallet(user.uid)
                        .onSuccess {
                            _uiState.update { it.copy(cryptoPortfolioError = null) }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    cryptoPortfolioError = error.localizedMessage
                                        ?: "Crypto portfolio could not be initialized."
                                )
                            }
                        }
                    mainWalletRepository.observeWallet(user.uid)
                        .catch { error ->
                            _uiState.update {
                                it.copy(errorMessage = error.localizedMessage ?: "Unable to read your user wallet")
                            }
                        }
                        .collect { wallet ->
                            _uiState.update { current ->
                                current.copy(
                                    totalBalanceUsd = wallet.usdBalance,
                                    totalBalanceKes = wallet.kesBalance,
                                    rewardClaimed = wallet.rewardClaimed,
                                    wallets = current.wallets.map { item ->
                                        when (item.currency) {
                                            Currency.USD -> item.copy(balanceAmount = wallet.usdBalance)
                                            Currency.KES -> item.copy(balanceAmount = wallet.kesBalance)
                                        }
                                    }
                                )
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(
                            userName = "Nova Trader",
                            userInitials = "NT",
                            cryptoPortfolioError = null,
                            totalBalanceUsd = 0.0,
                            totalBalanceKes = 0.0,
                            rewardClaimed = false,
                            wallets = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.observeUnreadCount()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            notificationErrorMessage = error.localizedMessage
                                ?: "Unable to load notification count."
                        )
                    }
                }
                .collect { count ->
                    _uiState.update {
                        it.copy(unreadNotificationsCount = count, notificationErrorMessage = null)
                    }
                }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentUser = authRepository.getCurrentUser()
            val fullName = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "Nova Trader"
            val publicWalletId = currentUser?.uid?.let { "NV-$it" }.orEmpty()
            val initials = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
            val mainWallet = currentUser?.let { user ->
                mainWalletRepository.observeWallet(user.uid).first()
            }
            val usdBalance = mainWallet?.usdBalance ?: 0.0
            val kesBalance = mainWallet?.kesBalance ?: 0.0
            val rewardClaimed = mainWallet?.rewardClaimed ?: false

            val userWallets = listOf(
                WalletUiModel(
                    id = "w1",
                    walletId = publicWalletId,
                    userName = fullName,
                    currency = Currency.USD,
                    balanceAmount = usdBalance,
                    cardNumberPlaceholder = "USD · TESTING",
                    isPrimary = true
                ),
                WalletUiModel(
                    id = "w2",
                    walletId = publicWalletId,
                    userName = fullName,
                    currency = Currency.KES,
                    balanceAmount = kesBalance,
                    cardNumberPlaceholder = "KES · TESTING",
                    isPrimary = false
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = fullName,
                    userInitials = initials.ifEmpty { "NT" },
                    totalBalanceUsd = usdBalance,
                    totalBalanceKes = kesBalance,
                    rewardClaimed = rewardClaimed,
                    wallets = userWallets,
                    recentTransactions = emptyList()
                )
            }
        }
    }

    fun selectCurrency(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }
}
