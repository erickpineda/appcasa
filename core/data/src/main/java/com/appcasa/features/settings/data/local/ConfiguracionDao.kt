package com.appcasa.features.settings.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT h.* FROM hogares h JOIN usuarios u ON h.id = u.hogar_id WHERE u.estado = 'ACTIVO' AND h.deleted_at IS NULL AND u.deleted_at IS NULL LIMIT 1")
    fun getHogarActual(): Flow<HogarEntity?>

    @Query("SELECT * FROM hogares WHERE id = :id AND deleted_at IS NULL LIMIT 1")
    fun getHogarById(id: String): Flow<HogarEntity?>

    @Query("SELECT * FROM hogares WHERE id = :id AND deleted_at IS NULL LIMIT 1")
    suspend fun getHogarByIdOnce(id: String): HogarEntity?

    @Query("SELECT * FROM hogares WHERE deleted_at IS NULL")
    fun getAllHogares(): Flow<List<HogarEntity>>

    @Query("SELECT * FROM hogares WHERE codigo_hogar = :code AND deleted_at IS NULL LIMIT 1")
    suspend fun getHogarByCodigo(code: String): HogarEntity?

    @Upsert
    suspend fun upsertHogar(hogar: HogarEntity)

    @Query("SELECT * FROM usuarios WHERE estado = 'ACTIVO' AND deleted_at IS NULL LIMIT 1")
    fun getUsuarioActual(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE deleted_at IS NULL")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE nombre = :nombre AND deleted_at IS NULL LIMIT 1")
    suspend fun getUsuarioByNombre(nombre: String): UsuarioEntity?

    @Upsert
    suspend fun upsertUsuario(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET estado = 'INACTIVO'")
    suspend fun deactivateAllUsers()

    @Query("UPDATE usuarios SET estado = 'ACTIVO' WHERE id = (SELECT id FROM usuarios WHERE hogar_id = :hogarId AND deleted_at IS NULL LIMIT 1)")
    suspend fun activateUserByHousehold(hogarId: String)

    @Query("UPDATE usuarios SET estado = 'ACTIVO' WHERE id = :userId")
    suspend fun activateUser(userId: String)

    @Query("UPDATE hogares SET codigo_hogar = :newCode, updated_at = :updatedAt WHERE id = :hogarId")
    suspend fun updateCodigoHogar(hogarId: String, newCode: String, updatedAt: Long)

    @Query("UPDATE hogares SET last_synced_at = :timestamp WHERE id = :hogarId")
    suspend fun updateHogarSyncTimestamp(hogarId: String, timestamp: Long)

    @Query("SELECT * FROM configuracion WHERE hogar_id = :hogarId AND deleted_at IS NULL")
    fun getConfiguracion(hogarId: String): Flow<List<ConfiguracionEntity>>

    @Upsert
    suspend fun upsertConfiguracion(config: ConfiguracionEntity)

    @Query("UPDATE usuarios SET deleted_at = :timestamp, deleted_by = :deletedBy")
    suspend fun softDeleteAllUsuarios(timestamp: Long, deletedBy: String)

    @Query("UPDATE hogares SET deleted_at = :timestamp, deleted_by = :deletedBy WHERE id = :id")
    suspend fun softDeleteHogar(id: String, timestamp: Long, deletedBy: String)

    @Query("UPDATE hogares SET deleted_at = :timestamp, deleted_by = :deletedBy")
    suspend fun softDeleteAllHogares(timestamp: Long, deletedBy: String)

    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()

    @Query("DELETE FROM hogares WHERE id = :id")
    suspend fun deleteHogar(id: String)

    @Query("DELETE FROM hogares")
    suspend fun deleteAllHogares()
}
