package com.appcasa.features.documents.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
  @Query("SELECT * FROM documentos WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY created_at DESC")
  fun getDocumentosByHogar(hogarId: String): Flow<List<DocumentoEntity>>

  @Query("SELECT * FROM documentos WHERE id = :id AND deleted_at IS NULL")
  suspend fun getDocumentoById(id: String): DocumentoEntity?

  @Query("UPDATE documentos SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateSyncTimestamp(id: String, timestamp: Long)

  @Query("UPDATE documentos SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
  suspend fun softDeleteDocumento(id: String, timestamp: Long, userId: String)

  @Upsert
  suspend fun upsertDocumento(documento: DocumentoEntity)

  @Delete
  suspend fun deleteDocumento(documento: DocumentoEntity)

  @Query("DELETE FROM documentos WHERE hogar_id = :hogarId")
  suspend fun deleteAllByHogar(hogarId: String)
}
