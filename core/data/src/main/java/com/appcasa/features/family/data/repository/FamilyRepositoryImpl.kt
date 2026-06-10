package com.appcasa.features.family.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.FamilyRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.mapper.toDomain
import com.appcasa.features.family.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val miembroDao: MiembroDao,
    private val remoteDataSource: FamilyRemoteDataSource,
    private val syncManager: SyncManager,
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

    override suspend fun updateMemberSyncTimestamp(memberId: Long) {
        miembroDao.updateSyncTimestamp(memberId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    remoteDataSource.observeMembers(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteMembers ->
                remoteMembers.forEach { remoteMember ->
                    val localMember = miembroDao.getMiembroById(remoteMember.id)
                    if (localMember == null || remoteMember.updatedAt > localMember.updatedAt) {
                        miembroDao.insertMiembro(remoteMember.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
