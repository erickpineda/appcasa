package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.HouseholdDto
import com.appcasa.core.domain.model.Household
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getHouseholdCollection() = firestore.collection("households")

    suspend fun syncHousehold(household: Household) {
        getHouseholdCollection().document(household.id.toString())
            .set(HouseholdDto.fromDomain(household)).await()
    }

    suspend fun getHouseholdByCode(code: String): Household? {
        val query = getHouseholdCollection()
            .whereEqualTo("codigoHogar", code)
            .limit(1)
            .get().await()
        
        return query.documents.firstOrNull()?.toObject(HouseholdDto::class.java)?.toDomain()
    }
}
