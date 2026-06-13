package com.appcasa.features.family.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.FamilyRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.mapper.toDomain
import com.appcasa.features.family.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class FamilyRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val miembroDao: MiembroDao,
    private val householdRepository: HouseholdRepository,
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

    override suspend fun getMemberBySyncId(syncId: String): FamilyMember? {
        return miembroDao.getMiembroBySyncId(syncId)?.toDomain()
    }

    override suspend fun updateMember(member: FamilyMember) {
        miembroDao.updateMiembro(member.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(member.hogarId)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        var memberToInsert = member

        // Resolve hogarSyncId
        if (memberToInsert.hogarSyncId == null && memberToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(memberToInsert.hogarId).first()
            memberToInsert = memberToInsert.copy(hogarSyncId = hogar?.syncId)
        }

        // Offline-first syncId
        if (memberToInsert.syncId == null) {
            memberToInsert = memberToInsert.copy(syncId = UUID.randomUUID().toString())
        }

        // Avoid duplicates
        val existing = memberToInsert.syncId?.let { miembroDao.getMiembroBySyncId(it) }
        if (existing != null) {
            memberToInsert = memberToInsert.copy(id = existing.id)
        }

        val id = miembroDao.insertMiembro(memberToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(memberToInsert.hogarId)
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeMembers(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteMembers ->
                remoteMembers.forEach { remoteMember ->
                    val existing = remoteMember.syncId?.let { miembroDao.getMiembroBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()

                    val memberToSave = remoteMember.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteMember.updatedAt > existing.updatedAt) {
                        miembroDao.insertMiembro(memberToSave.toEntity())
                    }
                }
            }
            .catch { e ->
                e.printStackTrace()
            }
            .launchIn(appScope)
    }
}
