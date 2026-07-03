package com.appcasa.core.domain.usecase.config

import com.appcasa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsCompactViewUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(hogarId: String): Flow<Boolean> {
        return repository.isCompactView(hogarId)
    }
}
