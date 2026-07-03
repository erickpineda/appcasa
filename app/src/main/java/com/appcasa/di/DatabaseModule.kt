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
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideDatabase(
    @ApplicationContext context: Context,
    passphrase: String
  ): AppCasaDatabase {
    val factory = SupportFactory(passphrase.toByteArray())
    return Room.databaseBuilder(
      context,
      AppCasaDatabase::class.java,
      "appcasa_db_secure"
    )
    .openHelperFactory(factory)
    .addMigrations(*Migrations.getAll())
    .fallbackToDestructiveMigration()
    .build()
  }

  @Provides
  fun provideHogarDao(db: AppCasaDatabase) = db.hogarDao()

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

  @Provides
  fun providePetDao(db: AppCasaDatabase) = db.petDao()

  @Provides
  fun provideRecompensaDao(db: AppCasaDatabase) = db.recompensaDao()
}
