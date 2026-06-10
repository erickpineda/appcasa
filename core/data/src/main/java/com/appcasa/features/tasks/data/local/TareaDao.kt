package com.appcasa.features.tasks.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 0 ORDER BY prioridad DESC, fecha_limite ASC")
    fun getTareasByHogar(hogarId: Long): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 0 ORDER BY prioridad DESC, fecha_limite ASC LIMIT :limit OFFSET :offset")
    fun getTareasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 1 ORDER BY completado_en DESC LIMIT :limit OFFSET :offset")
    fun getArchivedTareasPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<TareaEntity>>

    @Query("UPDATE tareas SET archived = 0 WHERE id = :id")
    suspend fun unarchiveTarea(id: Long)

    @Query("UPDATE tareas SET archived = 1 WHERE hogar_id = :hogarId AND estado = 'COMPLETADA' AND completado_en < :threshold")
    suspend fun archiveOldCompletedTasks(hogarId: Long, threshold: Long)

    @Query("DELETE FROM tareas WHERE hogar_id = :hogarId AND archived = 1")
    suspend fun deleteAllArchivedTasks(hogarId: Long)

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

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId")
    suspend fun getAsignacionesByTarea(tareaId: Long): List<TareaAsignacionEntity>

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId AND miembro_id = :miembroId")
    suspend fun getAsignacionByTareaAndMiembro(tareaId: Long, miembroId: Long): TareaAsignacionEntity?

    @Query("SELECT * FROM tarea_check_items WHERE tarea_id = :tareaId ORDER BY orden ASC")
    fun getCheckItems(tareaId: Long): Flow<List<TareaCheckItemEntity>>

    @Query("SELECT * FROM tarea_check_items WHERE id = :id")
    suspend fun getCheckItemById(id: Long): TareaCheckItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckItem(item: TareaCheckItemEntity): Long

    @Update
    suspend fun updateCheckItem(item: TareaCheckItemEntity)

    @Delete
    suspend fun deleteCheckItem(item: TareaCheckItemEntity)

    @Query("UPDATE tareas SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

    @Query("""
        SELECT tarea_id as taskId, COUNT(*) as total, SUM(CASE WHEN completado THEN 1 ELSE 0 END) as completed 
        FROM tarea_check_items 
        WHERE tarea_id IN (SELECT id FROM tareas WHERE hogar_id = :hogarId)
        GROUP BY tarea_id
    """)
    fun getAllCheckItemsCounts(hogarId: Long): Flow<List<TareaSubTaskCount>>
}

data class TareaSubTaskCount(
    val taskId: Long,
    val total: Int,
    val completed: Int
)
