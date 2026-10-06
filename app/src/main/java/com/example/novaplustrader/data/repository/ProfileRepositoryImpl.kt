package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.profile.models.ProfileModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileRepositoryImpl : ProfileRepository {

    private val _profile = MutableStateFlow<ProfileModel?>(ProfileModel())

    override fun observeProfile(): StateFlow<ProfileModel?> = _profile.asStateFlow()

    override suspend fun updateProfile(
        fullName: String,
        displayName: String,
        phoneNumber: String
    ): Result<Boolean> {
        delay(600) // Simulate backend persistence update
        _profile.update { current ->
            current?.copy(
                fullName = fullName,
                displayName = displayName,
                phoneNumber = phoneNumber,
                updatedAt = "Just now"
            )
        }
        return Result.success(true)
    }

    override suspend fun logout(): Result<Boolean> {
        delay(500)
        _profile.value = null
        return Result.success(true)
    }
}
