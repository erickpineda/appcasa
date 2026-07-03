package com.appcasa.features.lists.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ListaDao {
  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL")
  fun getListasByHogar(hogarId: String): Flow<List<ListaEntity>>

  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL LIMIT :limit OFFSET :offset")
  fun getListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<ListaEntity>>

  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL LIMIT :limit OFFSET :offset")
  fun getArchivedListasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<ListaEntity>>

  @Query("UPDATE listas SET archived = 0 WHERE id = :id")
  suspend fun unarchiveLista(id: String)

  @Query("UPDATE lista_items SET deleted_at = :timestamp, deleted_by = :userId WHERE lista_id = :listaId AND completado = 1 AND deleted_at IS NULL")
  suspend fun deleteCompletedItems(listaId: String, timestamp: Long, userId: String)

  @Query("UPDATE listas SET deleted_at = :timestamp, deleted_by = :userId WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL")
  suspend fun deleteAllArchivedListas(hogarId: String, timestamp: Long, userId: String)

  @Upsert
  suspend fun upsertLista(lista: ListaEntity)

  @Query("SELECT * FROM lista_items WHERE lista_id = :listaId AND deleted_at IS NULL")
  fun getItemsByLista(listaId: String): Flow<List<ListaItemEntity>>

  @Upsert
  suspend fun upsertItem(item: ListaItemEntity)

  @Query("SELECT * FROM lista_items WHERE id = :id AND deleted_at IS NULL")
  suspend fun getItemById(id: String): ListaItemEntity?

  @Upsert
  suspend fun upsertItems(items: List<ListaItemEntity>)

  @Query("UPDATE lista_items SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
  suspend fun softDeleteItem(id: String, timestamp: Long, userId: String)

  @Delete
  suspend fun deleteItem(item: ListaItemEntity)

  @Delete
  suspend fun deleteItems(items: List<ListaItemEntity>)

  @Query("UPDATE listas SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
  suspend fun softDeleteLista(id: String, timestamp: Long, userId: String)

  @Delete
  suspend fun deleteLista(lista: ListaEntity)

  @Query("SELECT * FROM listas WHERE id = :id AND deleted_at IS NULL")
  suspend fun getListById(id: String): ListaEntity?

  @Query("UPDATE listas SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateListSyncTimestamp(id: String, timestamp: Long)

  @Query("UPDATE lista_items SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateListItemSyncTimestamp(id: String, timestamp: Long)

  @Query("""
    SELECT i.* FROM lista_items i
    JOIN listas l ON i.lista_id = l.id
    WHERE l.hogar_id = :hogarId AND i.updated_at > COALESCE(i.last_synced_at, 0)
  """)
  suspend fun getItemsToSync(hogarId: String): List<ListaItemEntity>

  @Query("DELETE FROM listas")
  suspend fun deleteAll()
}
