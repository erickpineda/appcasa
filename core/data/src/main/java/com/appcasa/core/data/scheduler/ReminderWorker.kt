package com.appcasa.core.data.scheduler

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.appcasa.core.utils.NotificationHelper
import java.util.Calendar

class ReminderWorker(
  private val context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    val title = inputData.getString("title") ?: context.getString(com.appcasa.core.data.R.string.notif_generic_reminder_title)
    val message = inputData.getString("message") ?: context.getString(com.appcasa.core.data.R.string.notif_generic_reminder_msg)
    val id = inputData.getInt("id", 0)
    val timeMillis = inputData.getLong("timeMillis", 0L)

    // Lógica para eventos de "Todo el día"
    if (timeMillis > 0) {
      val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
      val isAllDay = cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
      
      // Si es todo el día y ya es tarde (ej: pasadas las 9 AM), no notificamos 
      // (evita notificaciones nocturnas si el worker se retrasa)
      if (isAllDay) {
        val now = Calendar.getInstance()
        if (now.get(Calendar.HOUR_OF_DAY) > 22) return Result.success()
      }
    }

    NotificationHelper.showNotification(context, id, title, message)

    return Result.success()
  }
}
