package com.appcasa.features.documents.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
  @Query("SELECT * FROM documentos WHERE hogar_id = :hogarId ORDER BY created_at DESC")
  fun getDocumentosByHogar(hogarId: Long): Flow<List<DocumentoEntity>>

  @Query("SELECT * FROM documentos WHERE id = :id")
  suspend fun getDocumentoById(id: Long): DocumentoEntity?

  @Query("SELECT * FROM documentos WHERE id = :id")
  suspend fun getDocumentById(id: Long): DocumentoEntity?

  @Query("UPDATE documentos SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocumento(documento: DocumentoEntity): Long

  @Delete
  suspend fun deleteDocumento(documento: DocumentoEntity)

  @Query("DELETE FROM documentos WHERE hogar_id = :hogarId")
  suspend fun deleteAllByHogar(hogarId: Long)
}
