package com.example.domain.models.services

import com.example.data.tables.repositories.RescateRepository
import com.example.domain.models.Rescate
import com.example.domain.models.RescateRequest
import java.util.UUID

class RescateService(private val repository: RescateRepository) {

    suspend fun getAllRescates() = repository.getAllRescates()

    suspend fun getRescateById(id: UUID) = repository.getRescateById(id)

    suspend fun getRescatesByAnimalito(animalitoId: UUID) =
        repository.getRescatesByAnimalito(animalitoId)

    suspend fun createRescate(request: RescateRequest): Rescate? {
        require(request.lugar.isNotBlank()) { "El lugar no puede estar vacío" }
        require(request.descripcion.isNotBlank()) { "La descripción no puede estar vacía" }
        return repository.createRescate(request)
    }

    suspend fun updateRescate(id: UUID, request: RescateRequest) =
        repository.updateRescate(id, request)

    suspend fun deleteRescate(id: UUID) = repository.deleteRescate(id)
}



