package com.appcasa.core.domain.model

data class Household(
    val id: Long = 0,
    val syncId: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val estado: EstadoGeneral = EstadoGeneral.ACTIVO,
    val codigoHogar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
) {
  companion object {
    const val CODE_PREFIX = "CASA-"
    const val CODE_PREFIX_LENGTH = 5
    const val CODE_SUFFIX_LENGTH = 4
    const val CODE_TOTAL_LENGTH = 9 // CODE_PREFIX_LENGTH + CODE_SUFFIX_LENGTH
  }
}
