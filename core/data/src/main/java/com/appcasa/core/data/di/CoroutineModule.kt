package com.appcasa.core.data.di

import com.appcasa.core.domain.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }
}
