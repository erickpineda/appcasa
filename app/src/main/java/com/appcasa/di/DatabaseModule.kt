package com.appcasa.di

import android.content.Context
import androidx.room.Room
import com.appcasa.core.data.local.AppCasaDatabase
import com.appcasa.core.data.local.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideAppCasaDatabase(
    @ApplicationContext context: Context
  ): AppCasaDatabase = Room.databaseBuilder(
    context,
    AppCasaDatabase::class.java,
    "appcasa.db"
  )
    .addMigrations(*Migrations.getAll())
    .fallbackToDestructiveMigration() // Mejor usar la opción global en desarrollo para evitar inconsistencias
    .build()

  @Provides
  fun provideConfiguracionDao(db: AppCasaDatabase) = db.hogarDao()

  @Provides
  fun provideMiembroDao(db: AppCasaDatabase) = db.miembroDao()

  @Provides
  fun provideTareaDao(db: AppCasaDatabase) = db.tareaDao()

  @Provides
  fun provideRecordatorioDao(db: AppCasaDatabase) = db.recordatorioDao()

  @Provides
  fun provideEventoDao(db: AppCasaDatabase) = db.eventoDao()

  @Provides
  fun provideListaDao(db: AppCasaDatabase) = db.listaDao()

  @Provides
  fun provideMascotaDao(db: AppCasaDatabase) = db.mascotaDao()

  @Provides
  fun provideUtilidadDao(db: AppCasaDatabase) = db.utilidadDao()

  @Provides
  fun provideStockDao(db: AppCasaDatabase) = db.stockDao()

  @Provides
  fun provideExpenseDao(db: AppCasaDatabase) = db.expenseDao()

  @Provides
  fun provideDocumentoDao(db: AppCasaDatabase) = db.documentoDao()

  @Provides
  fun provideDashboardDao(db: AppCasaDatabase) = db.dashboardDao()

  @Provides
  fun provideMaintenanceDao(db: AppCasaDatabase) = db.maintenanceDao()
}
