package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getRemindersByHogar(hogarId: Long): Flow<List<Reminder>>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun deleteReminder(reminder: Reminder)
}
