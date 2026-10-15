package com.example.novaplustrader.ui.screens.access

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.FeatureAccessRepository
import com.example.novaplustrader.data.repository.FeatureAccessRepositoryImpl
import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.EligibilityStatus
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.getStandardUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeatureAccessViewModel(
    private val repository: FeatureAccessRepository = FeatureAccessRepositoryImpl()
) : ViewModel() {

    private val _accessMap = MutableStateFlow<Map<AppFeature, FeatureAccessModel>>(emptyMap())
    val accessMap: StateFlow<Map<AppFeature, FeatureAccessModel>> = _accessMap.asStateFlow()

    init {
        observeAllFeatureAccess()
    }

    private fun observeAllFeatureAccess() {
        viewModelScope.launch {
            repository.observeAllFeatureAccess().collect { map ->
                _accessMap.value = map
            }
        }
    }

    fun getAccessForFeature(feature: AppFeature): FeatureAccessModel {
        return _accessMap.value[feature] ?: FeatureAccessModel(
            feature = feature,
            status = EligibilityStatus.AVAILABLE,
            userMessage = getStandardUserMessage(EligibilityStatus.AVAILABLE)
        )
    }
}
