package com.appcasa.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

  companion object {
    const val CHANNEL_ID = "appcasa_reminders"
    const val CHANNEL_NAME = "Alertas y Recordatorios"
    const val CHANNEL_DESC = "Notificaciones para eventos, tareas y recordatorios familiares."

    fun showNotification(context: Context, id: Int, title: String, message: String) {
      val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      
      val pendingIntent = intent?.let {
        PendingIntent.getActivity(
          context, 0, it,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
      }

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .apply {
          if (pendingIntent != null) {
            setContentIntent(pendingIntent)
          }
        }
        .build()

      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.notify(id, notification)
    }
  }

  init {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
        description = CHANNEL_DESC
        enableVibration(true)
      }
      val manager = context.getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }
}
