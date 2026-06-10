package com.appcasa.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Archivo central para todas las migraciones de la base de datos AppCasa.
 * Reiniciado a Versión 1 para consolidar el esquema inicial.
 */
object Migrations {

  /**
   * Lista de todas las migraciones registradas.
   * Actualmente vacía ya que el esquema se ha consolidado en la Versión 1.
   */
  fun getAll(): Array<Migration> {
    return arrayOf()
  }
}
