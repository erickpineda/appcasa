package com.appcasa.di

import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.providers.DefaultCurrentHouseholdProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {

    @Provides
    @Singleton
    fun provideCurrentHouseholdProvider(): CurrentHouseholdProvider {
        return DefaultCurrentHouseholdProvider()
    }
}
