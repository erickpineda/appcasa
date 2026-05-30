package com.appcasa.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Archivo central para todas las migraciones de la base de datos AppCasa.
 */
object Migrations {

  /**
   * Migración puente entre 8 y 9.
   */
  val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // Intentamos añadir la columna que faltaba según el logcat
      db.execSQL("ALTER TABLE tareas ADD COLUMN anticipacion_mins INTEGER NOT NULL DEFAULT 0")
    }
  }

  /**
   * Migración para añadir sistema de gamificación (puntos y nivel) a los miembros.
   */
  val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE miembros ADD COLUMN puntos INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE miembros ADD COLUMN nivel INTEGER NOT NULL DEFAULT 1")
    }
  }

  /**
   * Lista de todas las migraciones registradas.
   */
  fun getAll(): Array<Migration> {
    return arrayOf(
      MIGRATION_8_9,
      MIGRATION_9_10
    )
  }
}
