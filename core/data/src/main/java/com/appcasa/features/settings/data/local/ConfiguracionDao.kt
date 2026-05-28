package com.appcasa.features.settings.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM hogares LIMIT 1")
    fun getHogarActual(): Flow<HogarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHogar(hogar: HogarEntity): Long

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuarioActual(): Flow<UsuarioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM configuracion WHERE hogar_id = :hogarId")
    fun getConfiguracion(hogarId: Long): Flow<List<ConfiguracionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracion(config: ConfiguracionEntity): Long
}
