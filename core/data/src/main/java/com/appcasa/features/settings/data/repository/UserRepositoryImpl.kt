package com.appcasa.features.settings.data.repository

import com.appcasa.core.domain.model.User
import com.appcasa.core.domain.repository.UserRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return configuracionDao.getUsuarioActual().map { it?.toDomain() }
    }

    override suspend fun insertUser(user: User): Long {
        return configuracionDao.insertUsuario(user.toEntity())
    }

    override suspend fun deleteUsers() {
        configuracionDao.deleteAllUsuarios()
    }

    override suspend fun deactivateAllUsers() {
        configuracionDao.deactivateAllUsers()
    }

    override suspend fun activateUser(userId: Long) {
        configuracionDao.activateUser(userId)
    }

    override suspend fun activateUserByHousehold(householdId: Long) {
        configuracionDao.activateUserByHousehold(householdId)
    }
}
