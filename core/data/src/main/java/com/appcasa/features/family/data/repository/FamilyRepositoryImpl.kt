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
  @param:ApplicationScope private val appScope: CoroutineScope,
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
      val hogar = householdRepository.getHogarById(member.hogarId).first()
      val hSyncId = member.hogarSyncId ?: hogar?.syncId
      if (hSyncId != null) {
        remoteDataSource.deleteMember(hSyncId, member)
      }
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
      val hogar = householdRepository.getHogarById(member.hogarId).first()
      val hSyncId = member.hogarSyncId ?: hogar?.syncId ?: return

      // Aseguramos que tenemos el objeto más completo posible antes de subirlo
      val memberToSync = if (member.syncId == null) {
        miembroDao.getMiembroById(member.id)?.toDomain() ?: member
      } else {
        member
      }

      remoteDataSource.syncMember(hSyncId, memberToSync)
      updateMemberSyncTimestamp(memberToSync.id)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private val syncJobs = mutableMapOf<Long, Job>()

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun startRemoteSync(hogarId: Long) {
    syncJobs[hogarId]?.cancel()

    syncJobs[hogarId] = combine(
      syncManager.isAppInForeground,
      householdRepository.getHogarById(hogarId).filterNotNull()
    ) { isInForeground, hogar ->
      if (isInForeground && hogar.syncId != null) {
        hogar.syncId
      } else {
        null
      }
    }
    .flatMapLatest { syncId ->
      if (syncId != null) {
        remoteDataSource.observeMembers(syncId)
      } else {
        emptyFlow()
      }
    }
    .onEach { remoteMembers ->
      val hogar = householdRepository.getHogarById(hogarId).first() ?: return@onEach
      remoteMembers.forEach { remoteMember ->
        val existing = remoteMember.syncId?.let { miembroDao.getMiembroBySyncId(it) }

        val memberToSave = remoteMember.copy(
          id = existing?.id ?: 0L,
          hogarId = hogarId,
          hogarSyncId = hogar.syncId
        )

        if (existing == null || remoteMember.updatedAt > existing.updatedAt) {
          miembroDao.insertMiembro(memberToSave.toEntity())
        }
      }
    }
    .catch { e ->
      e.printStackTrace()
      syncJobs.remove(hogarId)
    }
    .onCompletion {
      syncJobs.remove(hogarId)
    }
    .launchIn(appScope)
  }
}
