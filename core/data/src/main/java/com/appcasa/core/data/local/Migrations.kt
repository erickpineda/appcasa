package com.appcasa.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Archivo central para todas las migraciones de la base de datos AppCasa.
 */
object Migrations {

  // Ejemplo de migración futura:
  // val MIGRATION_8_9 = object : Migration(8, 9) {
  //   override fun migrate(db: SupportSQLiteDatabase) {
  //     // SQL para actualizar tablas
  //   }
  // }

  /**
   * Lista de todas las migraciones registradas.
   */
  fun getAll(): Array<Migration> {
    return arrayOf(
      // MIGRATION_8_9
    )
  }
}
