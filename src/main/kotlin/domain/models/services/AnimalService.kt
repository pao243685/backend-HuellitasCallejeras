package com.example.domain.models.services

import com.example.data.tables.repositories.AnimalRepository
import com.example.domain.models.Animal
import com.example.domain.models.AnimalRequest
import com.example.domain.models.AnimalRescateResponse
import com.example.domain.models.RescateRequestSinAnimalId
import java.util.UUID

class AnimalService(private val repository: AnimalRepository) {

    suspend fun getAllAnimal(): List<Animal> {
        return repository.getAllAnimal()
    }

    suspend fun getAnimalById(id: UUID): Animal? {
        return repository.getAnimalById(id)
    }

    suspend fun deleteAnimal(id: UUID): Boolean {
        return repository.deleteAnimal(id)
    }

    private fun validateAnimalRequest(request: AnimalRequest) {
        require(request.nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(request.peso > 0) { "El peso debe ser mayor a 0" }
        require(request.edad > 0) { "La edad debe ser mayor a 0" }
        require(request.sexo in listOf("Macho", "Hembra")) { "Sexo inválido: ${request.sexo}" }
        require(request.especie.isNotBlank()) { "La especie no puede estar vacía" }
        require(request.estado in listOf("En recuperación", "En adopción", "Adoptado")) {
            "Estado inválido: ${request.estado}"
        }
    }

    suspend fun createAnimalConRescate(
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse? {
        try {
            validateAnimalRequest(animalRequest)

            require(rescateRequest.lugar.isNotBlank()) { "El lugar de rescate no puede estar vacío" }
            require(rescateRequest.descripcion.isNotBlank()) { "La descripción del rescate no puede estar vacía" }

            return repository.createAnimalConRescate(animalRequest, rescateRequest)

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateAnimalConRescate(
        animalId: UUID,
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse? {
        try {
            validateAnimalRequest(animalRequest)

            require(rescateRequest.lugar.isNotBlank()) { "El lugar de rescate no puede estar vacío" }
            require(rescateRequest.descripcion.isNotBlank()) { "La descripción del rescate no puede estar vacía" }

            return repository.updateAnimalConRescate(animalId, animalRequest, rescateRequest)

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAnimalConRescate(id: UUID): AnimalRescateResponse? {
        return repository.getAnimalConRescate(id)
    }
}