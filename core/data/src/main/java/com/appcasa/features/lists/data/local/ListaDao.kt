package com.appcasa.features.lists.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListaDao {
    @Query("SELECT * FROM listas WHERE hogar_id = :hogarId")
    fun getListasByHogar(hogarId: Long): Flow<List<ListaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLista(lista: ListaEntity): Long

    @Query("SELECT * FROM lista_items WHERE lista_id = :listaId")
    fun getItemsByLista(listaId: Long): Flow<List<ListaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ListaItemEntity): Long

    @Update
    suspend fun updateItem(item: ListaItemEntity)

    @Delete
    suspend fun deleteItem(item: ListaItemEntity)

    @Delete
    suspend fun deleteLista(lista: ListaEntity)

    @Query("DELETE FROM listas")
    suspend fun deleteAll()
}
