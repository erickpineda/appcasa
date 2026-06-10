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
        listaDao.insertLista(lista.toEntity())
        syncScheduler.scheduleSync(lista.hogarId)
    }

    override suspend fun deleteLista(lista: Lista) {
        listaDao.deleteLista(lista.toEntity())
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

    override suspend fun insertItem(item: ListaItem) {
        listaDao.insertItem(item.toEntity())
        // trigger sync for the list
    }

    override suspend fun updateItem(item: ListaItem) {
        listaDao.updateItem(item.toEntity())
    }

    override suspend fun updateItems(items: List<ListaItem>) {
        listaDao.updateItems(items.map { it.toEntity() })
    }

    override suspend fun deleteItem(item: ListaItem) {
        listaDao.deleteItem(item.toEntity())
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
                remoteLists.forEach { remoteList ->
                    val localList = listaDao.getListById(remoteList.id)
                    if (localList == null || remoteList.updatedAt > localList.updatedAt) {
                        listaDao.insertLista(remoteList.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
