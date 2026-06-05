package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import kotlinx.coroutines.flow.Flow

interface ListsRepository {
    fun getListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Lista>>
    fun getArchivedListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Lista>>
    suspend fun insertLista(lista: Lista)
    suspend fun deleteLista(lista: Lista)
    suspend fun unarchiveLista(listaId: Long)
    suspend fun deleteAllArchivedListas(hogarId: Long)
    suspend fun deleteCompletedItems(listaId: Long)
    
    // Items
    fun getItemsByLista(listaId: Long): Flow<List<ListaItem>>
    suspend fun insertItem(item: ListaItem)
    suspend fun updateItem(item: ListaItem)
    suspend fun deleteItem(item: ListaItem)
}
