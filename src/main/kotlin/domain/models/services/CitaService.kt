package com.example.domain.models.services

import com.example.data.tables.repositories.CitaRepository
import com.example.domain.models.Cita
import com.example.domain.models.CitaRequest
import java.util.UUID

class CitaService(private val repository: CitaRepository) {

    suspend fun getAllCitas() = repository.getAllCitas()

    suspend fun getCitaById(id: UUID) = repository.getCitaById(id)

    suspend fun getCitasByAnimal(animalId: UUID) =
        repository.getCitasByAnimal(animalId)

    suspend fun getCitasPendientes() = repository.getCitasPendientes()

    suspend fun createCita(request: CitaRequest): Cita? {
        require(request.motivo.isNotBlank()) { "El motivo no puede estar vacío" }
        require(request.lugar.isNotBlank()) { "El lugar no puede estar vacío" }
        return repository.createCita(request)
    }

    suspend fun updateCita(id: UUID, request: CitaRequest) =
        repository.updateCita(id, request)

    suspend fun deleteCita(id: UUID) = repository.deleteCita(id)
}