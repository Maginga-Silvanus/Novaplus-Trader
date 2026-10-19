package com.example.novaplustrader.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.CryptoWalletRepository
import com.example.novaplustrader.data.repository.CryptoWalletRepositoryImpl
import com.example.novaplustrader.data.repository.CryptoWalletSnapshot
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CryptoWalletUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snapshot: CryptoWalletSnapshot? = null
)

class CryptoWalletViewModel(
    private val cryptoRepository: CryptoWalletRepository = CryptoWalletRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoWalletUiState(isLoading = false))
    val uiState: StateFlow<CryptoWalletUiState> = _uiState.asStateFlow()

    init {
        loadCryptoWallet(silent = true)
    }

    fun loadCryptoWallet(silent: Boolean = false) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sign in to access your crypto wallet")
                }
                return@launch
            }
            if (!silent) {
                _uiState.update { it.copy(isLoading = true) }
            }
            cryptoRepository.ensureWallet(userId).onSuccess { snapshot ->
                _uiState.update { it.copy(isLoading = false, snapshot = snapshot) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }

            try {
                cryptoRepository.observeWallet(userId).collect { snapshot ->
                    // Silent background update without setting isLoading = true
                    _uiState.update { it.copy(snapshot = snapshot, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun refresh() {
        loadCryptoWallet(silent = false)
    }
}
