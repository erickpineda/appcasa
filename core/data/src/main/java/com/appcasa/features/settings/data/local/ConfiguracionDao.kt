package com.appcasa.features.settings.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT h.* FROM hogares h JOIN usuarios u ON h.id = u.hogar_id WHERE u.is_active = 1 LIMIT 1")
    fun getHogarActual(): Flow<HogarEntity?>

    @Query("SELECT * FROM hogares WHERE id = :id LIMIT 1")
    fun getHogarById(id: Long): Flow<HogarEntity?>

    @Query("SELECT * FROM hogares WHERE id = :id LIMIT 1")
    suspend fun getHogarByIdOnce(id: Long): HogarEntity?

    @Query("SELECT * FROM hogares")
    fun getAllHogares(): Flow<List<HogarEntity>>

    @Query("SELECT * FROM hogares WHERE codigo_hogar = :code LIMIT 1")
    suspend fun getHogarByCodigo(code: String): HogarEntity?

    @Query("SELECT * FROM hogares WHERE sync_id = :syncId LIMIT 1")
    suspend fun getHogarBySyncId(syncId: String): HogarEntity?

    @Upsert
    suspend fun insertHogar(hogar: HogarEntity): Long

    @Query("SELECT * FROM usuarios WHERE is_active = 1 LIMIT 1")
    fun getUsuarioActual(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Upsert
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Query("UPDATE usuarios SET is_active = 0")
    suspend fun deactivateAllUsers()

    @Query("UPDATE usuarios SET is_active = 1 WHERE id = (SELECT id FROM usuarios WHERE hogar_id = :hogarId LIMIT 1)")
    suspend fun activateUserByHousehold(hogarId: Long)

    @Query("UPDATE usuarios SET is_active = 1 WHERE id = :userId")
    suspend fun activateUser(userId: Long)

    @Query("UPDATE hogares SET codigo_hogar = :newCode, updated_at = :updatedAt WHERE id = :hogarId")
    suspend fun updateCodigoHogar(hogarId: Long, newCode: String, updatedAt: Long)

    @Query("UPDATE hogares SET last_synced_at = :timestamp WHERE id = :hogarId")
    suspend fun updateHogarSyncTimestamp(hogarId: Long, timestamp: Long)

    @Query("SELECT * FROM configuracion WHERE hogar_id = :hogarId")
    fun getConfiguracion(hogarId: Long): Flow<List<ConfiguracionEntity>>

    @Upsert
    suspend fun insertConfiguracion(config: ConfiguracionEntity): Long

    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()

    @Query("DELETE FROM hogares WHERE id = :id")
    suspend fun deleteHogar(id: Long)

    @Query("DELETE FROM hogares")
    suspend fun deleteAllHogares()
}
