package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.Reward
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRewardsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: String): Flow<List<Reward>> {
        return repository.getRewardsByHogar(hogarId)
    }
}

class AddRewardUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(hogarId: String, titulo: String, puntos: Int, desc: String?) {
        repository.upsertReward(
            Reward(
                hogarId = hogarId,
                titulo = titulo,
                costePuntos = puntos,
                descripcion = desc
            )
        )
    }
}

class RedeemRewardUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(memberId: String, reward: Reward): Boolean {
        val member = familyRepository.getMemberById(memberId)
        if (member != null && member.puntos >= reward.costePuntos) {
            familyRepository.updateMember(member.copy(
                puntos = member.puntos - reward.costePuntos,
                updatedAt = System.currentTimeMillis()
            ))
            return true
        }
        return false
    }
}

class DeleteRewardUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(reward: Reward) {
        repository.deleteReward(reward)
    }
}
