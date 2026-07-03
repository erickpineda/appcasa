package com.appcasa.features.tasks.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY prioridad DESC, fecha_limite ASC")
    fun getTareasByHogar(hogarId: String): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY prioridad DESC, fecha_limite ASC LIMIT :limit OFFSET :offset")
    fun getTareasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL ORDER BY completado_en DESC LIMIT :limit OFFSET :offset")
    fun getArchivedTareasPaged(hogarId: String, limit: Int, offset: Int): Flow<List<TareaEntity>>

    @Query("UPDATE tareas SET archived = 0 WHERE id = :id")
    suspend fun unarchiveTarea(id: String)

    @Query("UPDATE tareas SET archived = 1 WHERE hogar_id = :hogarId AND estado = 'COMPLETADA' AND completado_en < :threshold AND deleted_at IS NULL")
    suspend fun archiveOldCompletedTasks(hogarId: String, threshold: Long)

    @Query("UPDATE tareas SET deleted_at = :timestamp, deleted_by = :userId WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL")
    suspend fun softDeleteAllArchivedTasks(hogarId: String, timestamp: Long, userId: String)

    @Query("SELECT * FROM tareas WHERE id = :id AND deleted_at IS NULL")
    suspend fun getTareaById(id: String): TareaEntity?

    @Query("SELECT * FROM categorias_tarea WHERE id = :id AND deleted_at IS NULL")
    suspend fun getCategoriaById(id: String): CategoriaTareaEntity?

    @Query("SELECT * FROM tarea_check_items WHERE id = :id AND deleted_at IS NULL")
    suspend fun getCheckItemById(id: String): TareaCheckItemEntity?

    @Upsert
    suspend fun upsertTarea(tarea: TareaEntity)

    @Query("UPDATE tareas SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteTarea(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteTarea(tarea: TareaEntity)

    @Query("DELETE FROM tareas")
    suspend fun deleteAll()

    @Query("DELETE FROM categorias_tarea")
    suspend fun deleteAllCategorias()

    @Query("SELECT * FROM categorias_tarea WHERE hogar_id = :hogarId AND deleted_at IS NULL")
    fun getCategorias(hogarId: String): Flow<List<CategoriaTareaEntity>>

    @Upsert
    suspend fun upsertCategoria(categoria: CategoriaTareaEntity)

    @Query("UPDATE categorias_tarea SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteCategoria(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteCategoria(categoria: CategoriaTareaEntity)

    @Query("UPDATE categorias_tarea SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateCategoriaSyncTimestamp(id: String, timestamp: Long)

    @Upsert
    suspend fun upsertAsignacion(asignacion: TareaAsignacionEntity)

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId AND deleted_at IS NULL LIMIT 1")
    suspend fun getAsignacionByTarea(tareaId: String): TareaAsignacionEntity?

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId AND deleted_at IS NULL")
    suspend fun getAsignacionesByTarea(tareaId: String): List<TareaAsignacionEntity>

    @Query("SELECT * FROM tarea_asignaciones WHERE tarea_id = :tareaId AND miembro_id = :miembroId AND deleted_at IS NULL")
    suspend fun getAsignacionByTareaAndMiembro(tareaId: String, miembroId: String): TareaAsignacionEntity?

    @Query("SELECT * FROM tarea_check_items WHERE tarea_id = :tareaId AND deleted_at IS NULL ORDER BY orden ASC")
    fun getCheckItems(tareaId: String): Flow<List<TareaCheckItemEntity>>

    @Upsert
    suspend fun upsertCheckItem(item: TareaCheckItemEntity)

    @Query("UPDATE tarea_check_items SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteCheckItem(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteCheckItem(item: TareaCheckItemEntity)

    @Query("UPDATE tareas SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Query("UPDATE tarea_check_items SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateCheckItemSyncTimestamp(id: String, timestamp: Long)

    @Query("""
        SELECT i.* FROM tarea_check_items i
        JOIN tareas t ON i.tarea_id = t.id
        WHERE t.hogar_id = :hogarId AND i.updated_at > COALESCE(i.last_synced_at, 0)
    """)
    suspend fun getCheckItemsToSync(hogarId: String): List<TareaCheckItemEntity>

    @Query("""
        SELECT tarea_id as taskId, COUNT(*) as total, SUM(CASE WHEN completado THEN 1 ELSE 0 END) as completed 
        FROM tarea_check_items 
        WHERE tarea_id IN (SELECT id FROM tareas WHERE hogar_id = :hogarId AND deleted_at IS NULL) AND deleted_at IS NULL
        GROUP BY tarea_id
    """)
    fun getAllCheckItemsCounts(hogarId: String): Flow<List<TareaSubTaskCount>>
}

data class TareaSubTaskCount(
    val taskId: String,
    val total: Int,
    val completed: Int
)
