package com.appcasa.features.family.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.mapper.toDomain
import com.appcasa.features.family.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
    private val miembroDao: MiembroDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncScheduler: SyncScheduler
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
        syncScheduler.scheduleSync(member.hogarId)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        val id = miembroDao.insertMiembro(member.toEntity())
        syncScheduler.scheduleSync(member.hogarId)
        return id
    }

    override suspend fun deleteMember(member: FamilyMember) {
        miembroDao.deleteMiembro(member.toEntity())
        syncScheduler.scheduleSync(member.hogarId)
    }

    override fun startRemoteSync(hogarId: Long) {
        firestoreDataSource.observeMembers(hogarId) { remoteMembers ->
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                remoteMembers.forEach { remoteMember ->
                    val localMember = miembroDao.getMiembroById(remoteMember.id)
                    if (localMember == null || remoteMember.updatedAt > localMember.updatedAt) {
                        miembroDao.insertMiembro(remoteMember.toEntity())
                    }
                }
            }
        }
    }
}
