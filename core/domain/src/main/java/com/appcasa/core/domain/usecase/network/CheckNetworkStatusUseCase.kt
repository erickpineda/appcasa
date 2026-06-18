package com.appcasa.core.domain.usecase.network

import com.appcasa.core.domain.repository.NetworkRepository
import javax.inject.Inject

class CheckNetworkStatusUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    operator fun invoke(): Boolean {
        return repository.isNetworkAvailable()
    }
}
