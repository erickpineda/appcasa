package com.appcasa.features.tasks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId ORDER BY prioridad DESC, fecha_limite ASC")
    fun getTareasByHogar(hogarId: Long): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun getTareaById(id: Long): TareaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    @Update
    suspend fun updateTarea(tarea: TareaEntity)

    @Delete
    suspend fun deleteTarea(tarea: TareaEntity)

    @Query("DELETE FROM tareas")
    suspend fun deleteAll()

    @Query("DELETE FROM categorias_tarea")
    suspend fun deleteAllCategorias()

    // DAOs para categorías y sub-items podrían ir aquí o en ficheros separados
    @Query("SELECT * FROM categorias_tarea WHERE hogar_id = :hogarId")
    fun getCategorias(hogarId: Long): Flow<List<CategoriaTareaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaTareaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsignacion(asignacion: TareaAsignacionEntity)

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId LIMIT 1")
    suspend fun getAsignacionByTarea(tareaId: Long): TareaAsignacionEntity?

    @Query("SELECT * FROM tarea_check_items WHERE tarea_id = :tareaId ORDER BY orden ASC")
    fun getCheckItems(tareaId: Long): Flow<List<TareaCheckItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckItem(item: TareaCheckItemEntity): Long

    @Update
    suspend fun updateCheckItem(item: TareaCheckItemEntity)

    @Delete
    suspend fun deleteCheckItem(item: TareaCheckItemEntity)
}
