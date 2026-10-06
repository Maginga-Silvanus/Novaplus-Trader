package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.profile.models.ProfileModel
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    fun observeProfile(): StateFlow<ProfileModel?>
    suspend fun updateProfile(fullName: String, displayName: String, phoneNumber: String): Result<Boolean>
    suspend fun logout(): Result<Boolean>
}
