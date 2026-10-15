package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.EligibilityStatus
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import com.example.novaplustrader.domain.model.access.getStandardActionRoute
import com.example.novaplustrader.domain.model.access.getStandardUserMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FeatureAccessServiceImpl : FeatureAccessService {

    private val _configs = MutableStateFlow(
        AppFeature.entries.map { feature ->
            FeatureConfiguration(
                feature = feature,
                enabled = true,
                maintenanceMode = false
            )
        }
    )

    private val _eligibilityMap = MutableStateFlow<Map<AppFeature, FeatureAccessModel>>(
        AppFeature.entries.associateWith { feature ->
            FeatureAccessModel(
                feature = feature,
                status = EligibilityStatus.AVAILABLE,
                userMessage = getStandardUserMessage(EligibilityStatus.AVAILABLE),
                actionRoute = null
            )
        }
    )

    override fun getAllEligibility(userId: String): StateFlow<Map<AppFeature, FeatureAccessModel>> = _eligibilityMap.asStateFlow()
    override fun getFeatureConfigurations(): StateFlow<List<FeatureConfiguration>> = _configs.asStateFlow()

    override fun getEligibilityForFeature(userId: String, feature: AppFeature): Flow<FeatureAccessModel> {
        return _eligibilityMap.map { map -> map[feature] ?: createDefaultAccessModel(feature) }
    }

    override suspend fun updateFeatureConfiguration(
        config: FeatureConfiguration,
        adminUid: String
    ): Result<Boolean> {
        delay(500) // Backend admin feature toggle persistence & audit log generation

        _configs.update { list ->
            list.map { if (it.feature == config.feature) config else it }
        }

        val newStatus = when {
            !config.enabled -> EligibilityStatus.FEATURE_DISABLED
            config.maintenanceMode -> EligibilityStatus.MAINTENANCE
            else -> EligibilityStatus.AVAILABLE
        }

        _eligibilityMap.update { map ->
            val existing = map[config.feature] ?: createDefaultAccessModel(config.feature)
            val updated = existing.copy(
                status = newStatus,
                userMessage = getStandardUserMessage(newStatus),
                actionRoute = getStandardActionRoute(newStatus)
            )
            map + (config.feature to updated)
        }

        return Result.success(true)
    }

    private fun createDefaultAccessModel(feature: AppFeature): FeatureAccessModel {
        return FeatureAccessModel(
            feature = feature,
            status = EligibilityStatus.AVAILABLE,
            userMessage = getStandardUserMessage(EligibilityStatus.AVAILABLE)
        )
    }
}
