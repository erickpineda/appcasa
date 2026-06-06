package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getMembersByHogar(hogarId: Long): Flow<List<FamilyMember>>
    suspend fun getMemberById(id: Long): FamilyMember?
    suspend fun updateMember(member: FamilyMember)
    suspend fun insertMember(member: FamilyMember): Long
    suspend fun deleteMember(member: FamilyMember)
    fun startRemoteSync(hogarId: Long)
}
