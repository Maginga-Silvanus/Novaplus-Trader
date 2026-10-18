package com.example.novaplustrader.ui.screens.profile.models

data class ProfileModel(
    val uid: String = "",
    val fullName: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val accountId: String = "",
    val verificationStatus: String = "PENDING",
    val accountStatus: String = "ACTIVE",
    val createdAt: String = "",
    val updatedAt: String = ""
)
