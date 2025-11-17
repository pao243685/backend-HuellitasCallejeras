package com.example.domain.models.services

import com.example.data.tables.repositories.AnimalRepository
import com.example.domain.models.Animalito
import com.example.domain.models.AnimalitoRequest

class AnimalService(private val repository: AnimalRepository) {

    suspend fun getAllAnimalitos(): List<Animalito> {
        return repository.getAllAnimalitos()
    }

    suspend fun getAnimalitoById(id: Int): Animalito? {
        return repository.getAnimalitoById(id)
    }

    suspend fun createAnimalito(request: AnimalitoRequest): Animalito? {
        validateAnimalitoRequest(request)
        return repository.createAnimalito(request)
    }

    suspend fun updateAnimalito(id: Int, request: AnimalitoRequest): Boolean {
        validateAnimalitoRequest(request)
        return repository.updateAnimalito(id, request)
    }

    suspend fun deleteAnimalito(id: Int): Boolean {
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
}