package com.example.novaplustrader.ui.screens.security.models

data class SecurityActivityModel(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val deviceInfo: String,
    val status: String = "SUCCESS",
    val ipAddress: String = "197.232.12.4"
)

data class ActiveSessionModel(
    val id: String,
    val deviceName: String,
    val platform: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)
