package com.appcasa.core.data.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appcasa.core.domain.scheduler.ReminderScheduler
import java.util.concurrent.TimeUnit

class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

    override fun scheduleReminder(id: Int, title: String, message: String, timeInMillis: Long) {
        val delay = timeInMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putInt("id", id)
            .putString("title", title)
            .putString("message", message)
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
