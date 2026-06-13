package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.HouseholdDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Household
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getHouseholdCollection() = firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)

    suspend fun syncHousehold(household: Household) {
        val syncId = household.syncId ?: return // No sincronizamos si no hay syncId (debería tenerlo)
        getHouseholdCollection().document(syncId)
            .set(HouseholdDto.fromDomain(household)).await()
    }

    suspend fun getHouseholdByCode(code: String): Household? {
        val query = getHouseholdCollection()
            .whereEqualTo("codigoHogar", code)
            .limit(1)
            .get().await()
        
        val doc = query.documents.firstOrNull()
        return doc?.toObject(HouseholdDto::class.java)?.let { dto ->
            // Aseguramos que el syncId se recupere del ID del documento si no está en el campo
            dto.copy(syncId = dto.syncId ?: doc.id).toDomain()
        }
    }

    suspend fun findHouseholdsByUserEmail(email: String): List<Household> {
        val query = firestore.collectionGroup(FirestoreConstants.COL_MEMBERS)
            .whereEqualTo("email", email)
            .get().await()
            
        val houseIds = query.documents.mapNotNull { it.reference.parent.parent?.id }.toSet()
        val households = mutableListOf<Household>()
        
        for (id in houseIds) {
            val doc = getHouseholdCollection().document(id).get().await()
            doc.toObject(HouseholdDto::class.java)?.let { dto ->
                // Aseguramos que el syncId se recupere del ID del documento
                households.add(dto.copy(syncId = dto.syncId ?: id).toDomain())
            }
        }
        
        return households
    }

    suspend fun findHouseholdsByUserUid(uid: String): List<Household> {
        val query = firestore.collectionGroup(FirestoreConstants.COL_MEMBERS)
            .whereEqualTo("firebaseUid", uid)
            .get().await()
            
        val houseIds = query.documents.mapNotNull { it.reference.parent.parent?.id }.toSet()
        val households = mutableListOf<Household>()
        
        for (id in houseIds) {
            val doc = getHouseholdCollection().document(id).get().await()
            doc.toObject(HouseholdDto::class.java)?.let { dto ->
                // Aseguramos que el syncId se recupere del ID del documento
                households.add(dto.copy(syncId = dto.syncId ?: id).toDomain())
            }
        }
        
        return households
    }
}
