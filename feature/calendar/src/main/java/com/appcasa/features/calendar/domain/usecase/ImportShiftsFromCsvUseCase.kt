package com.appcasa.features.calendar.domain.usecase

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImportShiftsFromCsvUseCase @Inject constructor(
    private val repository: CalendarRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(hogarId: Long, content: String): Boolean {
        return try {
            val lines = content.lines()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            lines.forEach { line ->
              val parts = line.split(",")
              if (parts.size >= 2) {
                val dateStr = parts[0].trim()
                val title = parts[1].trim()
                val date = dateFormat.parse(dateStr)?.time
                if (date != null) {
                  val id = repository.insertEvent(
                    Event(
                      hogarId = hogarId,
                      titulo = "Turno: $title",
                      fecha = date,
                      tipo = TipoEvento.REUNION
                    )
                  )
                  
                  if (date > System.currentTimeMillis()) {
                    reminderScheduler.scheduleReminder(
                      id = (id + 10000).toInt(),
                      title = "Turno hoy: $title",
                      message = "Recuerda tu turno de trabajo para hoy",
                      timeInMillis = date + (8 * 60 * 60 * 1000) // 8 AM
                    )
                  }
                }
              }
            }
            true
          } catch (e: Exception) {
            e.printStackTrace()
            false
          }
    }
}
