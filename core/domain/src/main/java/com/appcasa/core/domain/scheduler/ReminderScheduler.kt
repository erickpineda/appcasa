package com.appcasa.core.domain.scheduler

interface ReminderScheduler {
    fun scheduleReminder(id: Int, title: String, message: String, timeInMillis: Long)
    fun cancelReminder(id: Int)
}
