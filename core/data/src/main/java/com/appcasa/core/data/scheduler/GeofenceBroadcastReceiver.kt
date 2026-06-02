package com.appcasa.core.data.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.appcasa.core.utils.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        
        if (geofencingEvent.hasError()) return

        val transitionType = geofencingEvent.geofenceTransition
        
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val id = intent.getIntExtra("id", 0)
            val title = intent.getStringExtra("title") ?: "Recordatorio de ubicación"
            val message = intent.getStringExtra("message") ?: "¡Estás cerca de un punto de interés!"
            
            NotificationHelper.showNotification(context, id, title, message)
        }
    }
}
