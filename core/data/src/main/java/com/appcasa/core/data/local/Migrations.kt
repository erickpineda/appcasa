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
   * Migración para añadir sistema de gamificación, estado de ánimo y dashboard personalizable.
   */
  val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // Estado de ánimo en miembros
      db.execSQL("ALTER TABLE miembros ADD COLUMN estado_animo TEXT")
      db.execSQL("ALTER TABLE miembros ADD COLUMN estado_animo_updated INTEGER")
      
      // Tabla Post-its (Ajustada para coincidir exactamente con el esquema esperado por Room)
      db.execSQL("""
        CREATE TABLE IF NOT EXISTS post_its (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          hogar_id INTEGER NOT NULL,
          contenido TEXT NOT NULL,
          color_hex TEXT NOT NULL,
          created_at INTEGER NOT NULL,
          FOREIGN KEY(hogar_id) REFERENCES hogares(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
      """.trimIndent())
      db.execSQL("CREATE INDEX IF NOT EXISTS index_post_its_hogar_id ON post_its (hogar_id)")
      
      // Tabla Configuración Dashboard
      db.execSQL("""
        CREATE TABLE IF NOT EXISTS dashboard_config (
          hogar_id INTEGER PRIMARY KEY NOT NULL,
          orden_modulos TEXT NOT NULL,
          FOREIGN KEY(hogar_id) REFERENCES hogares(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
      """.trimIndent())
      db.execSQL("CREATE INDEX IF NOT EXISTS index_dashboard_config_hogar_id ON dashboard_config (hogar_id)")
    }
  }

  /**
   * Migración para evitar duplicidad de puntos en tareas.
   */
  val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE tareas ADD COLUMN puntos_otorgados INTEGER NOT NULL DEFAULT 0")
    }
  }

  /**
   * Lista de todas las migraciones registradas.
   */
  fun getAll(): Array<Migration> {
    return arrayOf(
      MIGRATION_8_9,
      MIGRATION_9_10,
      MIGRATION_10_11,
      MIGRATION_11_12
    )
  }
}
