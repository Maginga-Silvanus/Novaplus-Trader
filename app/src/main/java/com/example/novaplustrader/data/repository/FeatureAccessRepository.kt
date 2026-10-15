package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import kotlinx.coroutines.flow.StateFlow

interface FeatureAccessRepository {
    fun observeFeatureAccess(feature: AppFeature): StateFlow<FeatureAccessModel>
    fun observeAllFeatureAccess(): StateFlow<Map<AppFeature, FeatureAccessModel>>
    fun observeFeatureConfigurations(): StateFlow<List<FeatureConfiguration>>

    suspend fun updateFeatureConfiguration(config: FeatureConfiguration, adminUid: String): Result<Boolean>
    suspend fun revalidateFeatureAccess(feature: AppFeature): Result<FeatureAccessModel>
}
