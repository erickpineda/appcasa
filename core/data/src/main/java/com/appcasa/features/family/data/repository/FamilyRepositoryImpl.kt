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
        miembroDao.updateMiembro(member.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(member.hogarId)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        val id = miembroDao.insertMiembro(member.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(member.hogarId)
        return id
    }

    override suspend fun deleteMember(member: FamilyMember) {
        miembroDao.deleteMiembro(member.toEntity())
        try {
            remoteDataSource.deleteMember(member)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(member.hogarId)
    }

    override suspend fun updateMemberSyncTimestamp(memberId: Long) {
        miembroDao.updateSyncTimestamp(memberId, System.currentTimeMillis())
    }

    override suspend fun addPointsToMember(memberId: Long, points: Int) {
        val member = miembroDao.getMiembroById(memberId)
        member?.let {
            val newPoints = it.puntos + points
            // Lógica simple de niveles: cada 100 puntos sube de nivel
            val newLevel = (newPoints / 100) + 1
            miembroDao.updateMiembro(it.copy(
                puntos = newPoints, 
                nivel = newLevel,
                updatedAt = System.currentTimeMillis()
            ))
            syncScheduler.scheduleSync(it.hogarId)
        }
    }

    override suspend fun syncMember(member: FamilyMember) {
        try {
            remoteDataSource.syncMember(member)
            updateMemberSyncTimestamp(member.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            .catch { e ->
                e.printStackTrace()
                // Evitamos que el error de permisos (PERMISSION_DENIED) cierre la app
            }
            .launchIn(appScope)
    }
}
