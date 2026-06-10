package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.Reward
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRewardsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<Reward>> {
        return repository.getRewardsByHogar(hogarId)
    }
}

class AddRewardUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(hogarId: Long, titulo: String, puntos: Int, desc: String?) {
        repository.insertReward(
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
    suspend operator fun invoke(memberId: Long, reward: Reward) {
        val member = familyRepository.getMemberById(memberId)
        if (member != null && member.puntos >= reward.costePuntos) {
            familyRepository.updateMember(member.copy(
                puntos = member.puntos - reward.costePuntos,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}

class DeleteRewardUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(reward: Reward) {
        repository.deleteReward(reward)
    }
}
