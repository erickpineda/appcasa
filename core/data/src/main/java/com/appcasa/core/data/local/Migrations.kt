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
   * Migración para añadir el módulo de Mantenimiento del Hogar.
   */
  val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("""
        CREATE TABLE IF NOT EXISTS mantenimiento_hogar (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          hogar_id INTEGER NOT NULL,
          titulo TEXT NOT NULL,
          descripcion TEXT,
          categoria TEXT NOT NULL,
          fecha_realizacion INTEGER NOT NULL,
          proxima_revision INTEGER,
          coste REAL,
          fotos_json TEXT,
          FOREIGN KEY(hogar_id) REFERENCES hogares(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
      """.trimIndent())
      db.execSQL("CREATE INDEX IF NOT EXISTS index_mantenimiento_hogar_hogar_id ON mantenimiento_hogar (hogar_id)")
    }
  }

  /**
   * Migración para optimizar rendimiento mediante índices en columnas críticas.
   */
  val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("CREATE INDEX IF NOT EXISTS index_miembros_nombre ON miembros (nombre)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_categoria ON stock (categoria)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_nombre ON stock (nombre)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_mantenimiento_hogar_fecha_realizacion ON mantenimiento_hogar (fecha_realizacion)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_mantenimiento_hogar_proxima_revision ON mantenimiento_hogar (proxima_revision)")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_gastos_fecha ON gastos (fecha)")
    }
  }

  /**
   * Migración para añadir la tienda de recompensas (Gamificación 2.0).
   */
  val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("""
        CREATE TABLE IF NOT EXISTS recompensas (
          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          hogar_id INTEGER NOT NULL,
          titulo TEXT NOT NULL,
          descripcion TEXT,
          coste_puntos INTEGER NOT NULL,
          icono TEXT NOT NULL,
          FOREIGN KEY(hogar_id) REFERENCES hogares(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
      """.trimIndent())
      db.execSQL("CREATE INDEX IF NOT EXISTS index_recompensas_hogar_id ON recompensas (hogar_id)")
    }
  }

  /**
   * Migración para añadir soporte de fotos/capturas a los gastos.
   */
  val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE gastos ADD COLUMN foto_uri TEXT")
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
      MIGRATION_11_12,
      MIGRATION_12_13,
      MIGRATION_13_14,
      MIGRATION_14_15,
      MIGRATION_15_16
    )
  }
}
