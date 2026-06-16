package com.appcasa

import android.app.Application
import net.sqlcipher.database.SQLiteDatabase
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.appcasa.features.dashboard.work.MaintenanceWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AppCasaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        scheduleMaintenanceWork()
    }

    private fun scheduleMaintenanceWork() {
        val workRequest = PeriodicWorkRequestBuilder<MaintenanceWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MaintenanceCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
