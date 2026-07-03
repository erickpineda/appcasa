package com.appcasa.features.lists.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.ListRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.domain.repository.ListsRepository
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.mapper.toDomain
import com.appcasa.features.lists.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class ListsRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val listaDao: ListaDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: ListRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : ListsRepository {

    override fun getListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Lista>> {
        return listaDao.getListasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Lista>> {
        return listaDao.getArchivedListasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertLista(lista: Lista) {
        listaDao.upsertLista(lista.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(lista.hogarId)
    }

    override suspend fun deleteLista(lista: Lista) {
        listaDao.deleteLista(lista.toEntity())
        syncScheduler.scheduleSync(lista.hogarId)
    }

    override suspend fun unarchiveLista(listaId: String) {
        listaDao.unarchiveLista(listaId)
    }

    override suspend fun deleteCompletedItems(listaId: String) {
        listaDao.deleteCompletedItems(listaId, System.currentTimeMillis(), "system")
    }

    override suspend fun deleteAllArchivedListas(hogarId: String) {
        listaDao.deleteAllArchivedListas(hogarId, System.currentTimeMillis(), "system")
    }

    override fun getItemsByLista(listaId: String): Flow<List<ListaItem>> {
        return listaDao.getItemsByLista(listaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertItem(item: ListaItem) {
        listaDao.upsertItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val list = listaDao.getListById(item.listaId)
        list?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun upsertItems(items: List<ListaItem>) {
        listaDao.upsertItems(items.map { it.copy(updatedAt = System.currentTimeMillis()).toEntity() })
    }

    override suspend fun deleteItem(item: ListaItem) {
        listaDao.deleteItem(item.toEntity())
        val list = listaDao.getListById(item.listaId)
        list?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun deleteItems(items: List<ListaItem>) {
        listaDao.deleteItems(items.map { it.toEntity() })
    }

    override suspend fun updateListSyncTimestamp(listaId: String) {
        listaDao.updateListSyncTimestamp(listaId, System.currentTimeMillis())
    }

    override suspend fun updateListItemSyncTimestamp(itemId: String) {
        listaDao.updateListItemSyncTimestamp(itemId, System.currentTimeMillis())
    }

    override suspend fun getItemsToSync(hogarId: String): List<ListaItem> {
        return listaDao.getItemsToSync(hogarId).map { it.toDomain() }
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4
    }

    private val itemSyncJobs = mutableMapOf<String, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncItems(hogarId: String, listId: String) {
        // TODO: Refactor in Phase 4
    }
}
