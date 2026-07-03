package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getMembersByHogar(hogarId: String): Flow<List<FamilyMember>>
    suspend fun getMemberById(id: String): FamilyMember?
    suspend fun getMemberBySyncId(syncId: String): FamilyMember?
    suspend fun updateMember(member: FamilyMember)
    suspend fun upsertMember(member: FamilyMember)
    suspend fun deleteMember(member: FamilyMember)
    suspend fun updateMemberSyncTimestamp(memberId: String)
    suspend fun addPointsToMember(memberId: String, points: Int)
    suspend fun syncMember(member: FamilyMember)
    fun startRemoteSync(hogarId: String)
}
