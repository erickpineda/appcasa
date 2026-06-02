package com.appcasa.core.data.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

  private val geofencingClient = LocationServices.getGeofencingClient(context)

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

  override fun scheduleLocationReminder(id: Int, title: String, message: String, latitude: Double, longitude: Double, radius: Float) {
    val geofence = Geofence.Builder()
      .setRequestId(id.toString())
      .setCircularRegion(latitude, longitude, radius)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
      .build()

    val geofencingRequest = GeofencingRequest.Builder()
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
      .addGeofence(geofence)
      .build()

    val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
      putExtra("id", id)
      putExtra("title", title)
      putExtra("message", message)
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
      context, 
      id, 
      intent, 
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      geofencingClient.addGeofences(geofencingRequest, pendingIntent)
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  override fun cancelReminder(id: Int) {
    WorkManager.getInstance(context).cancelUniqueWork("reminder_$id")
    geofencingClient.removeGeofences(listOf(id.toString()))
  }
}
