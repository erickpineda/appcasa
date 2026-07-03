package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import kotlinx.coroutines.flow.Flow

interface ListsRepository {
    fun getListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Lista>>
    fun getArchivedListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Lista>>
    suspend fun upsertLista(lista: Lista)
    suspend fun deleteLista(lista: Lista)
    suspend fun unarchiveLista(listaId: String)
    suspend fun deleteAllArchivedListas(hogarId: String)
    suspend fun deleteCompletedItems(listaId: String)
    
    // Items
    fun getItemsByLista(listaId: String): Flow<List<ListaItem>>
    suspend fun upsertItem(item: ListaItem)
    suspend fun upsertItems(items: List<ListaItem>)
    suspend fun deleteItem(item: ListaItem)
    suspend fun deleteItems(items: List<ListaItem>)
    suspend fun updateListSyncTimestamp(listaId: String)
    suspend fun updateListItemSyncTimestamp(itemId: String)
    suspend fun getItemsToSync(hogarId: String): List<ListaItem>
    fun startRemoteSync(hogarId: String)
}
