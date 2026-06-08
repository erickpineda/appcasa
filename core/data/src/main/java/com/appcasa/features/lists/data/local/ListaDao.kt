package com.appcasa.features.lists.data.local

import androidx.room.Dao
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLista(lista: ListaEntity): Long

    @Query("SELECT * FROM lista_items WHERE lista_id = :listaId")
    fun getItemsByLista(listaId: Long): Flow<List<ListaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ListaItemEntity): Long

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

    @Query("DELETE FROM listas")
    suspend fun deleteAll()
}
