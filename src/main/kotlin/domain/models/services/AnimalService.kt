package com.example.domain.models.services

import com.example.data.tables.repositories.AnimalRepository
import com.example.domain.models.Animalito
import com.example.domain.models.AnimalitoRequest
import com.example.domain.models.AnimalitoRescateResponse
import com.example.domain.models.RescateRequestSinAnimalitoId
import java.util.UUID

class AnimalService(private val repository: AnimalRepository) {

    suspend fun getAllAnimalitos(): List<Animalito> {
        return repository.getAllAnimalitos()
    }

    suspend fun getAnimalitoById(id: UUID): Animalito? {
        return repository.getAnimalitoById(id)
    }

    suspend fun createAnimalito(request: AnimalitoRequest): Animalito? {
        validateAnimalitoRequest(request)
        return repository.createAnimalito(request)
    }

    suspend fun updateAnimalito(id: UUID, request: AnimalitoRequest): Boolean {
        validateAnimalitoRequest(request)
        return repository.updateAnimalito(id, request)
    }

    suspend fun deleteAnimalito(id: UUID): Boolean {
        return repository.deleteAnimalito(id)
    }

    suspend fun getAnimalitosByEstado(estado: String): List<Animalito> {
        return repository.getAnimalitosByEstado(estado)
    }

    private fun validateAnimalitoRequest(request: AnimalitoRequest) {
        require(request.nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(request.peso > 0) { "El peso debe ser mayor a 0" }
        require(request.edad > 0) { "La edad debe ser mayor a 0" }
        require(request.sexo in listOf("Macho", "Hembra")) { "Sexo inválido" }
        require(request.especie.isNotBlank()) { "La especie no puede estar vacía" }
        require(request.estado in listOf("En rescate", "En tratamiento", "Disponible", "Adoptado")) {
            "Estado inválido"
        }
    }


    suspend fun createAnimalitoConRescate(
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse? {
        validateAnimalitoRequest(animalRequest)
        require(rescateRequest.lugar.isNotBlank()) { "El lugar de rescate no puede estar vacío" }
        require(rescateRequest.descripcion.isNotBlank()) { "La descripción del rescate no puede estar vacía" }

        return repository.createAnimalitoConRescate(animalRequest, rescateRequest)
    }

    suspend fun updateAnimalitoConRescate(
        animalId: UUID,
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse? {
        validateAnimalitoRequest(animalRequest)
        require(rescateRequest.lugar.isNotBlank()) { "El lugar de rescate no puede estar vacío" }
        require(rescateRequest.descripcion.isNotBlank()) { "La descripción del rescate no puede estar vacía" }

        return repository.updateAnimalitoConRescate(animalId, animalRequest, rescateRequest)
    }

    suspend fun getAnimalitoConRescate(id: UUID): AnimalitoRescateResponse? {
        return repository.getAnimalitoConRescate(id)
    }

}