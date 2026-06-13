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
        var listToInsert = lista
        if (listToInsert.hogarSyncId == null && listToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(listToInsert.hogarId).first()
            listToInsert = listToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (listToInsert.syncId == null) {
            listToInsert = listToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = listToInsert.syncId?.let { listaDao.getListBySyncId(it) }
        if (existing != null) {
            listToInsert = listToInsert.copy(id = existing.id)
        }
        listaDao.insertLista(listToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(listToInsert.hogarId)
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

    override suspend fun deleteCompletedItems(listaId: Long) {
        listaDao.deleteCompletedItems(listaId)
    }

    override suspend fun deleteAllArchivedListas(hogarId: Long) {
        listaDao.deleteAllArchivedListas(hogarId)
    }

    override fun getItemsByLista(listaId: Long): Flow<List<ListaItem>> {
        return listaDao.getItemsByLista(listaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertItem(item: ListaItem) {
        var itemToInsert = item
        if (itemToInsert.listaSyncId == null && itemToInsert.listaId > 0) {
            val list = listaDao.getListById(itemToInsert.listaId)
            itemToInsert = itemToInsert.copy(listaSyncId = list?.syncId)
        }
        if (itemToInsert.syncId == null) {
            itemToInsert = itemToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = itemToInsert.syncId?.let { listaDao.getItemBySyncId(it) }
        if (existing != null) {
            itemToInsert = itemToInsert.copy(id = existing.id)
        }
        listaDao.insertItem(itemToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val list = listaDao.getListById(itemToInsert.listaId)
        list?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun updateItem(item: ListaItem) {
        listaDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val list = listaDao.getListById(item.listaId)
        list?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun updateItems(items: List<ListaItem>) {
        listaDao.updateItems(items.map { it.copy(updatedAt = System.currentTimeMillis()).toEntity() })
    }

    override suspend fun deleteItem(item: ListaItem) {
        listaDao.deleteItem(item.toEntity())
        val list = listaDao.getListById(item.listaId)
        list?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun deleteItems(items: List<ListaItem>) {
        listaDao.deleteItems(items.map { it.toEntity() })
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeLists(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteLists ->
                remoteLists.forEach { remoteList ->
                    val existing = remoteList.syncId?.let { listaDao.getListBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    
                    val listToSave = remoteList.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteList.updatedAt > existing.updatedAt) {
                        listaDao.insertLista(listToSave.toEntity())
                    }
                    
                    remoteList.syncId?.let { 
                        observeAndSyncItems(hogarId, listToSave.id, it)
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private val itemSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncItems(hogarId: Long, listId: Long, listSyncId: String) {
        if (itemSyncJobs.containsKey(listId)) return
        
        itemSyncJobs[listId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeListItems(it, listSyncId) } ?: emptyFlow()
                }
                else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { listaDao.getItemBySyncId(it) }
                    val itemToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        listaId = listId,
                        listaSyncId = listSyncId
                    )
                    if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
                        listaDao.insertItem(itemToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
