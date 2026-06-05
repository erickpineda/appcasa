package com.appcasa.features.reminders.data.repository

import com.appcasa.core.domain.model.Reminder
import com.appcasa.core.domain.repository.ReminderRepository
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.mapper.toDomain
import com.appcasa.features.reminders.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val recordatorioDao: RecordatorioDao
) : ReminderRepository {

    override fun getRemindersByHogar(hogarId: Long): Flow<List<Reminder>> {
        return recordatorioDao.getRecordatoriosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return recordatorioDao.insertRecordatorio(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        recordatorioDao.deleteRecordatorio(reminder.toEntity())
    }
}
