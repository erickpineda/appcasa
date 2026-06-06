package com.appcasa.core.domain.usecase.user

import com.appcasa.core.domain.model.User
import com.appcasa.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getCurrentUser()
    }
}
