package com.appcasa.core.domain.usecase.config

import com.appcasa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencySymbolUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(hogarId: Long): Flow<String> {
        return repository.getCurrencySymbol(hogarId)
    }
}
