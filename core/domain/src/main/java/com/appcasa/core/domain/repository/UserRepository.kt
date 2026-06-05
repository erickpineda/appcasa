package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun insertUser(user: User): Long
    suspend fun deleteUsers()
    suspend fun deactivateAllUsers()
    suspend fun activateUser(userId: Long)
    suspend fun activateUserByHousehold(householdId: Long)
}
