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
   * Migración para añadir soporte de archivo a Tareas, Listas, Gastos y Mantenimiento (Optimización de rendimiento).
   */
  val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE tareas ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE listas ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE gastos ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE mantenimiento_hogar ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
    }
  }

  /**
   * Migración para añadir soporte multiusuario: código de hogar y auth_id.
   */
  val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE hogares ADD COLUMN codigo_hogar TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_hogares_codigo_hogar ON hogares (codigo_hogar)")
      db.execSQL("ALTER TABLE usuarios ADD COLUMN auth_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_usuarios_auth_id ON usuarios (auth_id)")
    }
  }

  /**
   * Migración para añadir atribución de autoría a tareas y gastos.
   */
  val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE tareas ADD COLUMN created_by_id INTEGER")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_tareas_created_by_id ON tareas (created_by_id)")
      db.execSQL("ALTER TABLE gastos ADD COLUMN created_by_id INTEGER")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_gastos_created_by_id ON gastos (created_by_id)")
    }
  }

  /**
   * Migración para añadir sync_id a las entidades restantes para sincronización total.
   */
  val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE stock ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_sync_id ON stock (sync_id)")
      
      db.execSQL("ALTER TABLE mantenimiento_hogar ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_mantenimiento_hogar_sync_id ON mantenimiento_hogar (sync_id)")
      
      db.execSQL("ALTER TABLE post_its ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_post_its_sync_id ON post_its (sync_id)")
      
      db.execSQL("ALTER TABLE recompensas ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_recompensas_sync_id ON recompensas (sync_id)")
      
      db.execSQL("ALTER TABLE documentos ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_documentos_sync_id ON documentos (sync_id)")

      db.execSQL("ALTER TABLE gastos ADD COLUMN sync_id TEXT")
      db.execSQL("CREATE INDEX IF NOT EXISTS index_gastos_sync_id ON gastos (sync_id)")
    }
  }

  /**
   * Migración para añadir is_active a usuarios para permitir multisesión local.
   */
  val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE usuarios ADD COLUMN is_active INTEGER NOT NULL DEFAULT 0")
      // Marcamos al primer usuario como activo para no romper sesiones existentes
      db.execSQL("UPDATE usuarios SET is_active = 1 WHERE id = (SELECT id FROM usuarios LIMIT 1)")
    }
  }

  /**
   * Migración para añadir miembro_id a usuarios para vincular Perfil con Miembro de familia.
   */
  val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE usuarios ADD COLUMN miembro_id INTEGER")
    }
  }

  /**
   * Migración para añadir rol a miembros para persistir permisos en el selector de perfiles.
   */
  val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE miembros ADD COLUMN rol TEXT NOT NULL DEFAULT 'COLABORADOR'")
      // Marcamos al primer miembro como ADMIN para mantener la lógica de creador
      db.execSQL("UPDATE miembros SET rol = 'ADMIN' WHERE id = (SELECT id FROM miembros ORDER BY id ASC LIMIT 1)")
    }
  }

  /**
   * Migración para añadir soporte de sincronización cloud (updated_at, last_synced_at).
   */
  val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE tareas ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE gastos ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      // Inicializar updated_at con created_at para gastos existentes
      db.execSQL("UPDATE gastos SET updated_at = created_at")
    }
  }

  /**
   * Migración para añadir soporte de sincronización cloud a Post-its (updated_at).
   */
  val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE post_its ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      // Inicializar con created_at
      db.execSQL("UPDATE post_its SET updated_at = created_at")
    }
  }

  /**
   * Migración para añadir soporte de sincronización (last_synced_at) a múltiples entidades.
   * También añade updated_at a mantenimiento y recompensas.
   */
  val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE gastos ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE miembros ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE stock ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE post_its ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE eventos ADD COLUMN last_synced_at INTEGER")
      
      db.execSQL("ALTER TABLE mantenimiento_hogar ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE mantenimiento_hogar ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE mantenimiento_hogar SET updated_at = fecha_realizacion")

      db.execSQL("ALTER TABLE recompensas ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE recompensas ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
    }
  }

  /**
   * Migración para completar la preparación de sincronización en todas las entidades restantes.
   */
  val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // Mascotas
      db.execSQL("ALTER TABLE mascota_vacunas ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE mascota_vacunas ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE mascota_vacunas SET updated_at = created_at")

      db.execSQL("ALTER TABLE mascota_desparasitaciones ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE mascota_desparasitaciones ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE mascota_desparasitaciones SET updated_at = created_at")

      db.execSQL("ALTER TABLE mascota_pesos ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE mascota_pesos ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE mascota_pesos SET updated_at = created_at")

      db.execSQL("ALTER TABLE mascota_medicaciones ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE mascota_medicaciones ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE mascota_medicaciones SET updated_at = created_at")

      // Documentos y Tareas
      db.execSQL("ALTER TABLE documentos ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE documentos ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE documentos SET updated_at = created_at")

      db.execSQL("ALTER TABLE categorias_tarea ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE categorias_tarea ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")

      db.execSQL("ALTER TABLE tarea_check_items ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE tarea_check_items ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE tarea_check_items SET updated_at = created_at")

      db.execSQL("ALTER TABLE tarea_asignaciones ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE tarea_asignaciones ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE tarea_asignaciones SET updated_at = created_at")

      // Dashboard Config
      db.execSQL("ALTER TABLE dashboard_config ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE dashboard_config ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")

      // Hogares y Usuarios
      db.execSQL("ALTER TABLE hogares ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE usuarios ADD COLUMN last_synced_at INTEGER")

      // Configuracion
      db.execSQL("ALTER TABLE configuracion ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE configuracion ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE configuracion ADD COLUMN sync_id TEXT")
      db.execSQL("ALTER TABLE configuracion ADD COLUMN last_synced_at INTEGER")

      // Listas
      db.execSQL("ALTER TABLE listas ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE lista_items ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE lista_items ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("UPDATE lista_items SET updated_at = created_at")

      // Utilidades y Recordatorios
      db.execSQL("ALTER TABLE utilidades ADD COLUMN last_synced_at INTEGER")
      db.execSQL("ALTER TABLE utilidades ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE utilidades ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE recordatorios ADD COLUMN last_synced_at INTEGER")
    }
  }

  /**
   * Migración para asegurar que todas las entidades tengan last_synced_at.
   * Se eliminan intentos redundantes de añadir columnas que ya existían desde v1.
   */
  val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // Solo añadimos lo que realmente falta y no estaba en versiones previas
      // Recordatorios: solo faltaba last_synced_at (añadido en 26-27).
      // created_at y updated_at ya estaban en v1.
      
      // Stock y Eventos ya tenían updated_at en v1/v4.
    }
  }

  /**
   * Migración puente para estabilizar la base de datos tras cambios masivos de sincronización.
   */
  val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // No hacemos cambios estructurales aquí, confiamos en fallbackToDestructiveMigration
      // si hay inconsistencias graves, pero intentamos mantener datos.
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
      MIGRATION_15_16,
      MIGRATION_16_17,
      MIGRATION_17_18,
      MIGRATION_18_19,
      MIGRATION_19_20,
      MIGRATION_20_21,
      MIGRATION_21_22,
      MIGRATION_22_23,
      MIGRATION_23_24,
      MIGRATION_24_25,
      MIGRATION_25_26,
      MIGRATION_26_27,
      MIGRATION_27_28,
      MIGRATION_28_29
    )
  }
}
