package com.appcasa.core.data.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appcasa.core.domain.scheduler.ReminderScheduler
import java.util.concurrent.TimeUnit
import java.util.Calendar

class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

  override fun scheduleReminder(id: Int, title: String, message: String, timeInMillis: Long) {
    var targetTime = timeInMillis
    val now = System.currentTimeMillis()
    
    val cal = Calendar.getInstance().apply { setTimeInMillis(targetTime) }
    val isAllDay = cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0

    // Si es "Todo el día" (00:00), ajustamos la notificación para las 9:00 AM de ese día
    if (isAllDay) {
      cal.set(Calendar.HOUR_OF_DAY, 9)
      cal.set(Calendar.MINUTE, 0)
      targetTime = cal.timeInMillis
    }

    val delay = targetTime - now
    if (delay <= 0) return

    val data = Data.Builder()
      .putInt("id", id)
      .putString("title", title)
      .putString("message", message)
      .putLong("timeMillis", targetTime)
      .build()

    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
      .setInitialDelay(delay, TimeUnit.MILLISECONDS)
      .setInputData(data)
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      "reminder_$id",
      ExistingWorkPolicy.REPLACE,
      workRequest
    )
  }

  override fun cancelReminder(id: Int) {
    WorkManager.getInstance(context).cancelUniqueWork("reminder_$id")
  }
}
