package com.appcasa.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import com.appcasa.features.dashboard.data.local.DashboardConfigEntity
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.local.PostItEntity
import com.appcasa.features.documents.data.local.DocumentoDao
import com.appcasa.features.documents.data.local.DocumentoEntity
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.data.local.ListaItemEntity
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.local.MaintenanceEntity
import com.appcasa.features.pets.data.local.MascotaDao
import com.appcasa.features.pets.data.local.MascotaDesparasitacionEntity
import com.appcasa.features.pets.data.local.MascotaMedicacionEntity
import com.appcasa.features.pets.data.local.MascotaPesoEntity
import com.appcasa.features.pets.data.local.MascotaVacunaEntity
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.local.RecordatorioEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import com.appcasa.features.tasks.data.local.CategoriaTareaEntity
import com.appcasa.features.tasks.data.local.RecompensaDao
import com.appcasa.features.tasks.data.local.RecompensaEntity
import com.appcasa.features.tasks.data.local.TareaAsignacionEntity
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.local.UtilidadEntity

/**
 * Base de datos central de AppCasa.
 *
 * Cada entidad vive en el paquete data/local de su módulo.
 * Este fichero solo registra las entidades y expone los DAOs.
 */
@Database(
  entities = [
    HogarEntity::class,
    UsuarioEntity::class,
    ConfiguracionEntity::class,
    MiembroEntity::class,
    CategoriaTareaEntity::class,
    TareaEntity::class,
    TareaAsignacionEntity::class,
    TareaCheckItemEntity::class,
    RecordatorioEntity::class,
    EventoEntity::class,
    ListaEntity::class,
    ListaItemEntity::class,
    MascotaVacunaEntity::class,
    MascotaDesparasitacionEntity::class,
    MascotaPesoEntity::class,
    MascotaMedicacionEntity::class,
    UtilidadEntity::class,
    StockEntity::class,
    ExpenseEntity::class,
    DocumentoEntity::class,
    PostItEntity::class,
    DashboardConfigEntity::class,
    MaintenanceEntity::class,
    RecompensaEntity::class,
  ],
  version      = 22,
  exportSchema = true
)
abstract class AppCasaDatabase : RoomDatabase() {

  abstract fun hogarDao(): ConfiguracionDao
  abstract fun miembroDao(): MiembroDao
  abstract fun tareaDao(): TareaDao
  abstract fun recordatorioDao(): RecordatorioDao
  abstract fun eventoDao(): EventoDao
  abstract fun listaDao(): ListaDao
  abstract fun mascotaDao(): MascotaDao
  abstract fun utilidadDao(): UtilidadDao
  abstract fun stockDao(): StockDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun documentoDao(): DocumentoDao
  abstract fun dashboardDao(): DashboardDao
  abstract fun maintenanceDao(): MaintenanceDao
  abstract fun recompensaDao(): RecompensaDao
}
