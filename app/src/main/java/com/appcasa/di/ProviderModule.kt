package com.appcasa.di

import com.appcasa.core.data.session.DatabaseSessionProvider
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun bindCurrentHouseholdProvider(
        impl: DatabaseSessionProvider
    ): CurrentHouseholdProvider
}
