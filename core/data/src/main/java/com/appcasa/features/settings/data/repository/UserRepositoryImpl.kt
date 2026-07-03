package com.appcasa.features.settings.data.repository

import com.appcasa.core.domain.model.User
import com.appcasa.core.domain.repository.UserRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return configuracionDao.getUsuarioActual().map { entity ->
            val domain = entity?.toDomain()
            if (domain != null) return@map domain

            // Si no hay usuario en Room, comprobamos Firebase
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Devolvemos un objeto de dominio VOLÁTIL (no está en la DB local aún)
                // Esto permite que el GlobalViewModel sepa que hay alguien "logueado"
                // pero que aún tiene que completar el setup del hogar.
                User(
                    id = "volatile_id", // ID indica que es volátil/no persistido
                    hogarId = "",
                    miembroId = "",
                    nombre = firebaseUser.displayName ?: "Usuario",
                    email = firebaseUser.email ?: "",
                    isActive = true
                )
            } else {
                null
            }
        }
    }

    override suspend fun getUserByName(name: String): User? {
        return configuracionDao.getUsuarioByNombre(name)?.toDomain()
    }

    override suspend fun insertUser(user: User): String {
        configuracionDao.upsertUsuario(user.toEntity())
        return user.id
    }

    override suspend fun deleteUsers() {
        configuracionDao.deleteAllUsuarios()
    }

    override suspend fun deactivateAllUsers() {
        configuracionDao.deactivateAllUsers()
    }

    override suspend fun activateUser(userId: String) {
        configuracionDao.activateUser(userId)
    }

    override suspend fun activateUserByHousehold(householdId: String) {
        configuracionDao.activateUserByHousehold(householdId)
    }
}
