package com.appcasa.features.lists.data.repository

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.mapper.toDomain
import com.appcasa.features.lists.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListsRepositoryImpl @Inject constructor(
    private val listaDao: ListaDao
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
    }

    override suspend fun deleteLista(lista: Lista) {
        listaDao.deleteLista(lista.toEntity())
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
    }

    override suspend fun updateItem(item: ListaItem) {
        listaDao.updateItem(item.toEntity())
    }

    override suspend fun deleteItem(item: ListaItem) {
        listaDao.deleteItem(item.toEntity())
    }
}
