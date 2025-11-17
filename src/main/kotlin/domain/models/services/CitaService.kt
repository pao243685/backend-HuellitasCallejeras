package com.example.domain.models.services

import com.example.data.tables.repositories.CitaRepository
import com.example.domain.models.Cita
import com.example.domain.models.CitaRequest

class CitaService(private val repository: CitaRepository) {

    suspend fun getAllCitas() = repository.getAllCitas()

    suspend fun getCitaById(id: Int) = repository.getCitaById(id)

    suspend fun getCitasByAnimalito(animalitoId: Int) =
        repository.getCitasByAnimalito(animalitoId)

    suspend fun getCitasPendientes() = repository.getCitasPendientes()

    suspend fun createCita(request: CitaRequest): Cita? {
        require(request.motivo.isNotBlank()) { "El motivo no puede estar vacío" }
        require(request.lugar.isNotBlank()) { "El lugar no puede estar vacío" }
        return repository.createCita(request)
    }

    suspend fun updateCita(id: Int, request: CitaRequest) =
        repository.updateCita(id, request)

    suspend fun deleteCita(id: Int) = repository.deleteCita(id)
}