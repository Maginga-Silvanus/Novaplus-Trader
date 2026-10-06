package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminBotModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBotsUiState(
    val bots: List<AdminBotModel> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL", // ALL, ACTIVE, PAUSED, STOPPED, ERROR
    val selectedBotForCommand: AdminBotModel? = null,
    val activeCommand: String? = null, // STOP, PAUSE, RESUME
    val isActionInProgress: Boolean = false,
    val isLoading: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminBotsViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBotsUiState())
    val uiState: StateFlow<AdminBotsUiState> = _uiState.asStateFlow()

    init {
        loadBots()
    }

    fun loadBots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val statusFilter = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter

            repository.getBots(
                searchQuery = _uiState.value.searchQuery,
                statusFilter = statusFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        bots = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadBots()
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadBots()
    }

    fun initiateBotCommand(bot: AdminBotModel, command: String) {
        _uiState.update {
            it.copy(
                selectedBotForCommand = bot,
                activeCommand = command
            )
        }
    }

    fun dismissCommandConfirmation() {
        _uiState.update {
            it.copy(
                selectedBotForCommand = null,
                activeCommand = null
            )
        }
    }

    fun confirmBotCommand() {
        val bot = _uiState.value.selectedBotForCommand ?: return
        val cmd = _uiState.value.activeCommand ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val result = repository.sendBotCommand(bot.botId, cmd)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedBotForCommand = null,
                        activeCommand = null,
                        actionSuccessMessage = "Bot command '$cmd' dispatched to backend bot execution engine."
                    )
                }
                loadBots()
            } else {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        errorMessage = "Bot command failed to execute on server."
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}
