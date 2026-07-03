package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getUserByName(name: String): User?
    suspend fun insertUser(user: User): String
    suspend fun deleteUsers()
    suspend fun deactivateAllUsers()
    suspend fun activateUser(userId: String)
    suspend fun activateUserByHousehold(householdId: String)
}
