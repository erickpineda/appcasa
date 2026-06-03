package com.appcasa.features.settings.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM hogares LIMIT 1")
    fun getHogarActual(): Flow<HogarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHogar(hogar: HogarEntity): Long

    @Query("SELECT * FROM usuarios WHERE is_active = 1 LIMIT 1")
    fun getUsuarioActual(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Query("UPDATE usuarios SET is_active = 0")
    suspend fun deactivateAllUsers()

    @Query("UPDATE usuarios SET is_active = 1 WHERE id = :userId")
    suspend fun activateUser(userId: Long)

    @Query("SELECT * FROM configuracion WHERE hogar_id = :hogarId")
    fun getConfiguracion(hogarId: Long): Flow<List<ConfiguracionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracion(config: ConfiguracionEntity): Long

    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()

    @Query("DELETE FROM hogares")
    suspend fun deleteAllHogares()
}
