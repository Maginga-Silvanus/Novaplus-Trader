package com.example.novaplustrader.ui.screens.gaming

import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.domain.model.gaming.GamingActivityModel
import com.example.novaplustrader.domain.model.gaming.ResponsibleGamingProfile

data class ResponsibleGamingUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val profile: ResponsibleGamingProfile = ResponsibleGamingProfile(),
    val activityLogs: List<GamingActivityModel> = emptyList(),

    // Self-Exclusion Flow Form
    val selectedExclusionPeriod: ExclusionPeriod = ExclusionPeriod.SEVEN_DAYS,
    val exclusionReasonInput: String = "Personal Choice",
    val isExclusionConfirmOpen: Boolean = false,
    val isExclusionSuccess: Boolean = false,

    // Reality Check Timer
    val sessionDurationFormatted: String = "00:32:15",
    val isRealityCheckAlertVisible: Boolean = false
)
