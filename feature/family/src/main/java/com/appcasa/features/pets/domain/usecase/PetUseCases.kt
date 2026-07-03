package com.appcasa.features.pets.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetWeightsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petId: String): Flow<List<PetWeight>> {
        return repository.getPesos(petId)
    }
}

class AddPetWeightUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String, value: Double) {
        repository.insertPeso(PetWeight(mascotaId = petId, pesoKg = value))
    }
}

class DeletePetWeightUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(weight: PetWeight) {
        repository.deletePeso(weight)
    }
}

class GetPetVaccinesUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petId: String): Flow<List<PetVaccine>> {
        return repository.getVacunas(petId)
    }
}

class AddPetVaccineUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String, name: String) {
        repository.insertVacuna(PetVaccine(mascotaId = petId, nombre = name))
    }
}

class DeletePetVaccineUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(vaccine: PetVaccine) {
        repository.deleteVacuna(vaccine)
    }
}

class GetPetMedicationsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petId: String): Flow<List<PetMedication>> {
        return repository.getMedicacionesActivas(petId)
    }
}

class AddPetMedicationUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String, name: String, dose: String, freq: String) {
        repository.insertMedicacion(PetMedication(mascotaId = petId, nombre = name, dosis = dose, frecuencia = freq))
    }
}

class UpdatePetMedicationUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(med: PetMedication) {
        repository.insertMedicacion(med)
    }
}

class DeletePetMedicationUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(med: PetMedication) {
        repository.deleteMedicacion(med)
    }
}

class GetPetDewormingsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petId: String): Flow<List<PetDeworming>> {
        return repository.getDesparasitaciones(petId)
    }
}

class AddPetDewormingUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String, type: String, product: String) {
        repository.insertDesparasitacion(PetDeworming(mascotaId = petId, tipo = type, producto = product))
    }
}

class DeletePetDewormingUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(item: PetDeworming) {
        repository.deleteDesparasitacion(item)
    }
}
