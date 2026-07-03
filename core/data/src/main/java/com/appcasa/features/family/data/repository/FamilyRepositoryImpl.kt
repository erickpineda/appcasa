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
import java.util.UUID
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
  @param:ApplicationScope private val appScope: CoroutineScope,
  private val miembroDao: MiembroDao,
  private val householdRepository: HouseholdRepository,
  private val remoteDataSource: FamilyRemoteDataSource,
  private val syncManager: SyncManager,
  private val syncScheduler: SyncScheduler
) : FamilyRepository {

  override fun getMembersByHogar(hogarId: String): Flow<List<FamilyMember>> {
    return miembroDao.getMiembrosByHogar(hogarId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun getMemberById(id: String): FamilyMember? {
    return miembroDao.getMiembroById(id)?.toDomain()
  }

  override suspend fun getMemberBySyncId(syncId: String): FamilyMember? {
    return null
  }

  override suspend fun updateMember(member: FamilyMember) {
    val updatedMember = member.copy(updatedAt = System.currentTimeMillis())
    miembroDao.upsertMiembro(updatedMember.toEntity())
    try {
      remoteDataSource.saveMember(updatedMember)
      miembroDao.updateSyncTimestamp(member.id, System.currentTimeMillis())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override suspend fun upsertMember(member: FamilyMember) {
    val idToUse = if (member.id.isBlank()) UUID.randomUUID().toString() else member.id
    val memberToInsert = member.copy(id = idToUse, updatedAt = System.currentTimeMillis())
    
    miembroDao.upsertMiembro(memberToInsert.toEntity())
    try {
      remoteDataSource.saveMember(memberToInsert)
      miembroDao.updateSyncTimestamp(idToUse, System.currentTimeMillis())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override suspend fun deleteMember(member: FamilyMember) {
    miembroDao.deleteMiembro(member.toEntity())
    try {
      remoteDataSource.deleteMember(member.hogarId, member.id)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override suspend fun updateMemberSyncTimestamp(memberId: String) {
    miembroDao.updateSyncTimestamp(memberId, System.currentTimeMillis())
  }

  override suspend fun addPointsToMember(memberId: String, points: Int) {
    val member = miembroDao.getMiembroById(memberId)
    member?.let {
      val newPoints = it.puntos + points
      val newLevel = (newPoints / 100) + 1
      val updated = it.copy(
        puntos = newPoints,
        nivel = newLevel,
        updatedAt = System.currentTimeMillis()
      )
      miembroDao.upsertMiembro(updated)
      try {
        remoteDataSource.saveMember(updated.toDomain())
        miembroDao.updateSyncTimestamp(memberId, System.currentTimeMillis())
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override suspend fun syncMember(member: FamilyMember) {
    try {
      remoteDataSource.saveMember(member)
      miembroDao.updateSyncTimestamp(member.id, System.currentTimeMillis())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun startRemoteSync(hogarId: String) {
    appScope.launch {
      try {
        val remoteMembers = remoteDataSource.getMembersByHousehold(hogarId)
        remoteMembers.forEach { member ->
          miembroDao.upsertMiembro(member.toEntity())
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
