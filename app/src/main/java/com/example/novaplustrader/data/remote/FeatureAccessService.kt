package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FeatureAccessService {
    fun getEligibilityForFeature(userId: String, feature: AppFeature): Flow<FeatureAccessModel>
    fun getAllEligibility(userId: String): StateFlow<Map<AppFeature, FeatureAccessModel>>
    fun getFeatureConfigurations(): StateFlow<List<FeatureConfiguration>>
    suspend fun updateFeatureConfiguration(config: FeatureConfiguration, adminUid: String): Result<Boolean>
}
