package com.appcasa.di

import android.content.Context
import com.appcasa.core.data.scheduler.WorkManagerReminderScheduler
import com.appcasa.core.domain.scheduler.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchedulerModule {

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context): ReminderScheduler {
        return WorkManagerReminderScheduler(context)
    }
}
