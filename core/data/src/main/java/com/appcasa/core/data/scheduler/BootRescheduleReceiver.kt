package com.appcasa.core.data.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.reminders.data.local.RecordatorioDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reprograma todos los recordatorios futuros tras un reinicio del dispositivo.
 */
@AndroidEntryPoint
class BootRescheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordatorioDao: RecordatorioDao

    @Inject
    lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val now = System.currentTimeMillis()
                    val futureReminders = recordatorioDao.getAllFutureReminders(now)
                    
                    futureReminders.forEach { reminder ->
                        scheduler.scheduleReminder(
                            id = reminder.id.toInt(),
                            title = reminder.titulo,
                            message = reminder.descripcion ?: "Recordatorio",
                            timeInMillis = reminder.fechaHora
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
