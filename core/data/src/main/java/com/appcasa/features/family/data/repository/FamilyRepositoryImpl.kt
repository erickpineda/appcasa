package com.appcasa.features.family.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.mapper.toDomain
import com.appcasa.features.family.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
    private val miembroDao: MiembroDao,
    private val firestoreDataSource: FirestoreDataSource
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
        firestoreDataSource.syncMember(member)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        val id = miembroDao.insertMiembro(member.toEntity())
        firestoreDataSource.syncMember(member.copy(id = id))
        return id
    }

    override suspend fun deleteMember(member: FamilyMember) {
        miembroDao.deleteMiembro(member.toEntity())
        // En un caso real marcaríamos como eliminado en remoto
    }
}
