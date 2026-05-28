package com.appcasa.core.data.scheduler

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.appcasa.core.utils.NotificationHelper

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Recordatorio AppCasa"
        val message = inputData.getString("message") ?: "Tienes una tarea pendiente"
        val id = inputData.getInt("id", 0)

        // Asumimos que NotificationHelper fue refactorizado para enviar la notificación directamente
        // En este diseño, NotificationHelper.showNotification debe existir
        NotificationHelper.showNotification(context, id, title, message)

        return Result.success()
    }
}
