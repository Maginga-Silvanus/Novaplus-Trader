package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.FeatureAccessService
import com.example.novaplustrader.data.remote.FeatureAccessServiceImpl
import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.EligibilityStatus
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import com.example.novaplustrader.domain.model.access.getStandardUserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeatureAccessRepositoryImpl(
    private val service: FeatureAccessService = FeatureAccessServiceImpl()
) : FeatureAccessRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _accessMap = MutableStateFlow<Map<AppFeature, FeatureAccessModel>>(
        AppFeature.entries.associateWith { feature ->
            FeatureAccessModel(
                feature = feature,
                status = EligibilityStatus.AVAILABLE,
                userMessage = getStandardUserMessage(EligibilityStatus.AVAILABLE)
            )
        }
    )

    init {
        scope.launch {
            service.getAllEligibility("usr_9901").collect { map ->
                _accessMap.value = map
            }
        }
    }

    override fun observeAllFeatureAccess(): StateFlow<Map<AppFeature, FeatureAccessModel>> = _accessMap.asStateFlow()

    override fun observeFeatureAccess(feature: AppFeature): StateFlow<FeatureAccessModel> {
        val currentModel = _accessMap.value[feature] ?: createDefaultModel(feature)
        return MutableStateFlow(currentModel).asStateFlow()
    }

    override fun observeFeatureConfigurations(): StateFlow<List<FeatureConfiguration>> {
        return service.getFeatureConfigurations()
    }

    override suspend fun updateFeatureConfiguration(
        config: FeatureConfiguration,
        adminUid: String
    ): Result<Boolean> {
        return service.updateFeatureConfiguration(config, adminUid)
    }

    override suspend fun revalidateFeatureAccess(feature: AppFeature): Result<FeatureAccessModel> {
        val current = _accessMap.value[feature] ?: createDefaultModel(feature)
        return Result.success(current)
    }

    private fun createDefaultModel(feature: AppFeature): FeatureAccessModel {
        return FeatureAccessModel(
            feature = feature,
            status = EligibilityStatus.AVAILABLE,
            userMessage = getStandardUserMessage(EligibilityStatus.AVAILABLE)
        )
    }
}
