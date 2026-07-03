package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getRemindersByHogar(hogarId: String): Flow<List<Reminder>>
    suspend fun upsertReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
}
