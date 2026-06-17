package com.appcasa.features.lists.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListaDao {
  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 0")
  fun getListasByHogar(hogarId: Long): Flow<List<ListaEntity>>

  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 0 LIMIT :limit OFFSET :offset")
  fun getListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<ListaEntity>>

  @Query("SELECT * FROM listas WHERE hogar_id = :hogarId AND archived = 1 LIMIT :limit OFFSET :offset")
  fun getArchivedListasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<ListaEntity>>

  @Query("UPDATE listas SET archived = 0 WHERE id = :id")
  suspend fun unarchiveLista(id: Long)

  @Query("DELETE FROM lista_items WHERE lista_id = :listaId AND completado = 1")
  suspend fun deleteCompletedItems(listaId: Long)

  @Query("DELETE FROM listas WHERE hogar_id = :hogarId AND archived = 1")
  suspend fun deleteAllArchivedListas(hogarId: Long)

  @Upsert
  suspend fun insertLista(lista: ListaEntity): Long

  @Query("SELECT * FROM lista_items WHERE lista_id = :listaId")
  fun getItemsByLista(listaId: Long): Flow<List<ListaItemEntity>>

  @Upsert
  suspend fun insertItem(item: ListaItemEntity): Long

  @Query("SELECT * FROM lista_items WHERE id = :id")
  suspend fun getItemById(id: Long): ListaItemEntity?

  @Update
  suspend fun updateItem(item: ListaItemEntity)

  @Transaction
  @Update
  suspend fun updateItems(items: List<ListaItemEntity>)

  @Delete
  suspend fun deleteItem(item: ListaItemEntity)

  @Transaction
  @Delete
  suspend fun deleteItems(items: List<ListaItemEntity>)

  @Delete
  suspend fun deleteLista(lista: ListaEntity)

  @Query("SELECT * FROM listas WHERE id = :id")
  suspend fun getListById(id: Long): ListaEntity?

  @Query("SELECT * FROM listas WHERE sync_id = :syncId LIMIT 1")
  suspend fun getListBySyncId(syncId: String): ListaEntity?

  @Query("SELECT * FROM lista_items WHERE sync_id = :syncId LIMIT 1")
  suspend fun getItemBySyncId(syncId: String): ListaItemEntity?

  @Query("UPDATE listas SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateListSyncTimestamp(id: Long, timestamp: Long)

  @Query("UPDATE lista_items SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateListItemSyncTimestamp(id: Long, timestamp: Long)

  @Query("""
    SELECT i.* FROM lista_items i
    JOIN listas l ON i.lista_id = l.id
    WHERE l.hogar_id = :hogarId AND i.updated_at > COALESCE(i.last_synced_at, 0)
  """)
  suspend fun getItemsToSync(hogarId: Long): List<ListaItemEntity>

  @Query("DELETE FROM listas")
  suspend fun deleteAll()
}
