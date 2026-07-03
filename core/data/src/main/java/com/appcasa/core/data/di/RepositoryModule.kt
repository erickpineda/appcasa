package com.appcasa.core.data.di

import com.appcasa.core.domain.repository.*
import com.appcasa.features.calendar.data.repository.CalendarRepositoryImpl
import com.appcasa.features.dashboard.data.repository.DashboardRepositoryImpl
import com.appcasa.features.documents.data.repository.DocumentRepositoryImpl
import com.appcasa.features.family.data.repository.FamilyRepositoryImpl
import com.appcasa.features.finance.data.repository.FinanceRepositoryImpl
import com.appcasa.features.inventory.data.repository.InventoryRepositoryImpl
import com.appcasa.features.lists.data.repository.ListsRepositoryImpl
import com.appcasa.features.maintenance.data.repository.MaintenanceRepositoryImpl
import com.appcasa.features.pets.data.repository.PetRepositoryImpl
import com.appcasa.features.reminders.data.repository.ReminderRepositoryImpl
import com.appcasa.features.settings.data.repository.ConfigurationRepositoryImpl
import com.appcasa.features.settings.data.repository.HouseholdRepositoryImpl
import com.appcasa.features.settings.data.repository.SettingsRepositoryImpl
import com.appcasa.features.settings.data.repository.UserRepositoryImpl
import com.appcasa.features.tasks.data.repository.TasksRepositoryImpl
import com.appcasa.features.utilities.data.repository.UtilityRepositoryImpl
import com.appcasa.core.data.repository.NetworkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        networkRepositoryImpl: NetworkRepositoryImpl
    ): NetworkRepository

    @Binds
    @Singleton
    abstract fun bindListsRepository(
        listsRepositoryImpl: ListsRepositoryImpl
    ): ListsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTasksRepository(
        tasksRepositoryImpl: TasksRepositoryImpl
    ): TasksRepository

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(
        familyRepositoryImpl: FamilyRepositoryImpl
    ): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindFinanceRepository(
        financeRepositoryImpl: FinanceRepositoryImpl
    ): FinanceRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        inventoryRepositoryImpl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindConfigurationRepository(
        configurationRepositoryImpl: ConfigurationRepositoryImpl
    ): ConfigurationRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        documentRepositoryImpl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindHouseholdRepository(
        householdRepositoryImpl: HouseholdRepositoryImpl
    ): HouseholdRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        maintenanceRepositoryImpl: MaintenanceRepositoryImpl
    ): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        petRepositoryImpl: PetRepositoryImpl
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindUtilityRepository(
        utilityRepositoryImpl: UtilityRepositoryImpl
    ): UtilityRepository

}
