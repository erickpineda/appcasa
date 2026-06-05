package com.appcasa.features.family.data.repository

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.mapper.toDomain
import com.appcasa.features.family.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
    private val miembroDao: MiembroDao
) : FamilyRepository {

    override fun getMembersByHogar(hogarId: Long): Flow<List<FamilyMember>> {
        return miembroDao.getMiembrosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemberById(id: Long): FamilyMember? {
        return miembroDao.getMiembroById(id)?.toDomain()
    }

    override suspend fun updateMember(member: FamilyMember) {
        miembroDao.updateMiembro(member.toEntity())
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        return miembroDao.insertMiembro(member.toEntity())
    }

    override suspend fun deleteMember(member: FamilyMember) {
        miembroDao.deleteMiembro(member.toEntity())
    }
}
