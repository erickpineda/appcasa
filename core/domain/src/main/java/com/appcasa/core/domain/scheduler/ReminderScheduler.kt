package com.appcasa.core.domain.scheduler

interface ReminderScheduler {
    fun scheduleReminder(id: Int, title: String, message: String, timeInMillis: Long)
    fun scheduleLocationReminder(id: Int, title: String, message: String, latitude: Double, longitude: Double, radius: Float = 200f)
    fun cancelReminder(id: Int)
}
