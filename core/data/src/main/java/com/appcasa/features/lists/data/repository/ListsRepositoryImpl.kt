package com.appcasa.features.lists.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.ListRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.mapper.toDomain
import com.appcasa.features.lists.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ListsRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val listaDao: ListaDao,
    private val remoteDataSource: ListRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : ListsRepository {

    override fun getListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Lista>> {
        return listaDao.getListasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Lista>> {
        return listaDao.getArchivedListasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertLista(lista: Lista) {
        listaDao.insertLista(lista.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(lista.hogarId)
    }

    override suspend fun deleteLista(lista: Lista) {
        listaDao.deleteLista(lista.toEntity())
        try {
            remoteDataSource.deleteList(lista)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(lista.hogarId)
    }

    override suspend fun unarchiveLista(listaId: Long) {
        listaDao.unarchiveLista(listaId)
    }

    override suspend fun deleteAllArchivedListas(hogarId: Long) {
        listaDao.deleteAllArchivedListas(hogarId)
    }

    override suspend fun deleteCompletedItems(listaId: Long) {
        listaDao.deleteCompletedItems(listaId)
    }

    override fun getItemsByLista(listaId: Long): Flow<List<ListaItem>> {
        return listaDao.getItemsByLista(listaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private suspend fun getHogarId(listaId: Long): Long {
        return listaDao.getListById(listaId)?.hogarId ?: 0L
    }

    override suspend fun insertItem(item: ListaItem) {
        listaDao.insertItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(item.listaId))
    }

    override suspend fun updateItem(item: ListaItem) {
        listaDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(item.listaId))
    }

    override suspend fun updateItems(items: List<ListaItem>) {
        val now = System.currentTimeMillis()
        val updated = items.map { it.copy(updatedAt = now) }
        listaDao.updateItems(updated.map { it.toEntity() })
        if (updated.isNotEmpty()) {
            syncScheduler.scheduleSync(getHogarId(updated.first().listaId))
        }
    }

    override suspend fun deleteItem(item: ListaItem) {
        val hogarId = getHogarId(item.listaId)
        listaDao.deleteItem(item.toEntity())
        try {
            remoteDataSource.deleteListItem(hogarId, item)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun deleteItems(items: List<ListaItem>) {
        if (items.isEmpty()) return
        val hogarId = getHogarId(items.first().listaId)
        listaDao.deleteItems(items.map { it.toEntity() })
        items.forEach { item ->
            try {
                remoteDataSource.deleteListItem(hogarId, item)
            } catch (e: Exception) { e.printStackTrace() }
        }
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun updateListSyncTimestamp(listaId: Long) {
        listaDao.updateListSyncTimestamp(listaId, System.currentTimeMillis())
    }

    override suspend fun updateListItemSyncTimestamp(itemId: Long) {
        listaDao.updateListItemSyncTimestamp(itemId, System.currentTimeMillis())
    }

    override suspend fun getItemsToSync(hogarId: Long): List<ListaItem> {
        return listaDao.getItemsToSync(hogarId).map { it.toDomain() }
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    remoteDataSource.observeLists(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteLists ->
                val remoteIds = remoteLists.map { it.id }.toSet()
                val localLists = listaDao.getListasPaged(hogarId, 1000, 0).first()
                
                localLists.forEach { local ->
                    if (local.id !in remoteIds) {
                        listaDao.deleteLista(local)
                    }
                }

                remoteLists.forEach { remoteList ->
                    val localList = listaDao.getListById(remoteList.id)
                    if (localList == null || remoteList.updatedAt > localList.updatedAt) {
                        listaDao.insertLista(remoteList.toEntity())
                    }
                    
                    // Sincronizar ítems de forma reactiva para cada lista (simplificado)
                    observeAndSyncItems(hogarId, remoteList.id)
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private val itemSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncItems(hogarId: Long, listaId: Long) {
        if (itemSyncJobs.containsKey(listaId)) return
        
        itemSyncJobs[listaId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) remoteDataSource.observeListItems(hogarId, listaId)
                else emptyFlow()
            }
            .onEach { remoteItems ->
                val remoteIds = remoteItems.map { it.id }.toSet()
                val localItems = listaDao.getItemsByLista(listaId).first()
                
                localItems.forEach { local ->
                    if (local.id !in remoteIds) {
                        listaDao.deleteItem(local)
                    }
                }

                remoteItems.forEach { remoteItem ->
                    val localItem = listaDao.getItemById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        listaDao.insertItem(remoteItem.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
