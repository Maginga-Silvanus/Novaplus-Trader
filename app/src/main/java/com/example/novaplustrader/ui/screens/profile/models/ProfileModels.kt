package com.example.novaplustrader.ui.screens.profile.models

data class ProfileModel(
    val uid: String = "usr_current_uid",
    val fullName: String = "Leonard Lopez",
    val displayName: String = "Leonard",
    val email: String = "leonard.lopez@novapay.io",
    val phoneNumber: String = "+254 712 345 678",
    val photoUrl: String? = null,
    val accountId: String = "#NV-92841",
    val verificationStatus: String = "VERIFIED",
    val accountStatus: String = "ACTIVE",
    val createdAt: String = "Member since Jan 2024",
    val updatedAt: String = "Today"
)
