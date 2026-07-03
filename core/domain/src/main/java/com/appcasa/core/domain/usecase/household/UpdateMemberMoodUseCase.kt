package com.appcasa.core.domain.usecase.household

import javax.inject.Inject

class UpdateMemberMoodUseCase @Inject constructor(
    private val getMemberByIdUseCase: GetMemberByIdUseCase,
    private val updateMemberUseCase: UpdateMemberUseCase
) {
    suspend operator fun invoke(miembroId: String, emoji: String?) {
        val miembro = getMemberByIdUseCase(miembroId)
        miembro?.let {
            updateMemberUseCase(it.copy(
                estadoAnimo = emoji
            ))
        }
    }
}
