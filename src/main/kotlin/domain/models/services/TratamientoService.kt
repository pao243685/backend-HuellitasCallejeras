package com.example.domain.models.services

import com.example.data.tables.repositories.TratamientoRepository
import com.example.domain.models.MedicamentoTratamiento
import com.example.domain.models.Tratamiento
import com.example.domain.models.TratamientoRequest
import java.util.UUID

class TratamientoService(private val repository: TratamientoRepository) {

    suspend fun getAllTratamientos() = repository.getAllTratamientos()

    suspend fun getTratamientoById(id: UUID) = repository.getTratamientoById(id)

    suspend fun getTratamientosByAnimal(animalId: UUID) =
        repository.getTratamientosByAnimal(animalId)

    suspend fun createTratamiento(request: TratamientoRequest): Tratamiento? {
        require(request.receta.isNotBlank()) { "La receta no puede estar vacía" }
        require(request.medicamentos.isNotEmpty()) { "Debe incluir al menos un medicamento" }
        request.medicamentos.forEach { med ->
            require(med.dosis > 0) { "La dosis debe ser mayor a 0" }
            require(med.repeticion >= 0) { "La repetición debe ser mayor o igual a 0" }
        }
        return repository.createTratamiento(request)
    }

    suspend fun updateTratamiento(id: UUID, receta: String?): Boolean {
        val current = repository.getTratamientoById(id) ?: return false
        val finalReceta = receta?.takeIf { it.isNotBlank() } ?: current.receta

        return repository.updateTratamiento(id, finalReceta)
    }

    suspend fun deleteTratamiento(id: UUID) = repository.deleteTratamiento(id)

    suspend fun getMedicamentosByTratamiento(tratamientoId: UUID) =
        repository.getMedicamentosByTratamiento(tratamientoId)

    suspend fun updateMedicamentos(id: UUID, medicamentos: List<MedicamentoTratamiento>): Boolean {
        require(medicamentos.isNotEmpty()) { "Debe incluir al menos un medicamento" }

        medicamentos.forEach { med ->
            require(med.dosis > 0) { "La dosis debe ser mayor a 0" }
            require(med.repeticion >= 0) { "La repetición debe ser mayor o igual a 0" }
        }

        return repository.replaceMedicamentos(id, medicamentos)
    }
}