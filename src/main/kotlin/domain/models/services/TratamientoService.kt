package com.example.domain.models.services

import com.example.data.tables.repositories.TratamientoRepository
import com.example.domain.models.Tratamiento
import com.example.domain.models.TratamientoRequest
import java.util.UUID

class TratamientoService(private val repository: TratamientoRepository) {

    suspend fun getAllTratamientos() = repository.getAllTratamientos()

    suspend fun getTratamientoById(id: UUID) = repository.getTratamientoById(id)

    suspend fun getTratamientosByAnimal(animalId: UUID) =
        repository.getTratamientosByAnimal(animalId)

    suspend fun createTratamiento(request: TratamientoRequest): Tratamiento? {
        println("🔵 [DEBUG-SERVICE] Validando TratamientoRequest...")
        println("🔵 [DEBUG-SERVICE] request: $request")

        require(request.receta.isNotBlank()) { "La receta no puede estar vacía" }
        require(request.medicamentos.isNotEmpty()) { "Debe incluir al menos un medicamento" }

        println("🔵 [DEBUG-SERVICE] Validando ${request.medicamentos.size} medicamentos...")

        request.medicamentos.forEach { med ->
            println("🔵 [DEBUG-SERVICE] Validando medicamento: $med")
            require(med.dosis > 0) { "La dosis debe ser mayor a 0" }
            require(med.repeticion >= 0) { "La repetición debe ser mayor o igual a 0" }
        }

        println("🔵 [DEBUG-SERVICE] Llamando a repository.createTratamiento...")
        return repository.createTratamiento(request)
    }

    suspend fun updateTratamiento(id: UUID, request: TratamientoRequest): Boolean {
        println("🔵 [DEBUG-SERVICE] Actualizando tratamiento ID: $id")
        println("🔵 [DEBUG-SERVICE] Request recibido: $request")

        require(request.receta.isNotBlank()) { "La receta no puede estar vacía" }
        require(request.medicamentos.isNotEmpty()) { "Debe incluir al menos un medicamento" }

        println("🔵 [DEBUG-SERVICE] Validando ${request.medicamentos.size} medicamentos...")

        request.medicamentos.forEach { med ->
            println("🔵 [DEBUG-SERVICE] Validando medicamento: $med")
            require(med.dosis > 0) { "La dosis debe ser mayor a 0" }
            require(med.repeticion >= 0) { "La repetición debe ser mayor o igual a 0" }
        }

        println("🔵 [DEBUG-SERVICE] Llamando a repository.updateTratamiento...")
        return repository.updateTratamiento(id, request)
    }

    suspend fun deleteTratamiento(id: UUID) = repository.deleteTratamiento(id)

    suspend fun getMedicamentosByTratamiento(tratamientoId: UUID) =
        repository.getMedicamentosByTratamiento(tratamientoId)
}