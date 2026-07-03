package com.appcasa.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.appcasa.MainActivity
import com.appcasa.R
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostItWidgetProvider : AppWidgetProvider() {

  @Inject
  lateinit var dashboardDao: DashboardDao

  @Inject
  lateinit var configuracionDao: ConfiguracionDao

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_post_it)
        
    CoroutineScope(Dispatchers.IO).launch {
      val hogar = configuracionDao.getHogarActual().firstOrNull()
      val hogarId = hogar?.id ?: ""
            
      val postIts = if (hogarId.isNotEmpty()) dashboardDao.getPostIts(hogarId).first() else emptyList()
      val lastPostIt = postIts.firstOrNull()?.contenido ?: context.getString(R.string.widget_no_notes)
            
      views.setTextViewText(R.id.widget_text, lastPostIt)
            
      val intent = Intent(context, MainActivity::class.java)
      val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
      views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
