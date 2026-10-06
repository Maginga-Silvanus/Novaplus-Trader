package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminUserModel
import com.example.novaplustrader.ui.screens.admin.models.AdminUserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUsersUiState(
    val users: List<AdminUserModel> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL", // ALL, ACTIVE, SUSPENDED, LOCKED, PENDING, CLOSED
    val selectedVerificationFilter: String = "ALL", // ALL, VERIFIED, UNVERIFIED, PENDING
    val sortBy: String = "NAME",
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminUsersViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val statusEnum = when (_uiState.value.selectedStatusFilter.uppercase()) {
                "ACTIVE" -> AdminUserStatus.ACTIVE
                "SUSPENDED" -> AdminUserStatus.SUSPENDED
                "LOCKED" -> AdminUserStatus.LOCKED
                "PENDING" -> AdminUserStatus.PENDING
                "CLOSED" -> AdminUserStatus.CLOSED
                else -> null
            }

            val verifFilter = if (_uiState.value.selectedVerificationFilter == "ALL") null else _uiState.value.selectedVerificationFilter

            repository.getUsers(
                searchQuery = _uiState.value.searchQuery,
                statusFilter = statusEnum,
                verificationFilter = verifFilter,
                sortBy = _uiState.value.sortBy,
                page = _uiState.value.currentPage
            ).collect { userList ->
                _uiState.update {
                    it.copy(
                        users = userList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadUsers()
    }

    fun onStatusFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedStatusFilter = filter) }
        loadUsers()
    }

    fun onVerificationFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedVerificationFilter = filter) }
        loadUsers()
    }

    fun onSortBySelected(sort: String) {
        _uiState.update { it.copy(sortBy = sort) }
        loadUsers()
    }
}
