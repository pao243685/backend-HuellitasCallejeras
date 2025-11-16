package com.example.services

import com.example.Routing.request.AnimalRequest
import com.example.Routing.response.AnimalResponse
import com.example.models.Animal
import com.example.repository.AnimalRepositorio
import java.util.UUID


class AnimalService(private val repository: AnimalRepositorio) {

    suspend fun getAll(): List<AnimalResponse> =
        repository.getAll().map { it.toResponse() }

    suspend fun add(request: AnimalRequest) {
        val newAnimal = Animal(
            idAnimal = UUID.randomUUID(),
            nombre = request.nombre,
            especie = request.especie,
            raza = request.raza,
            sexo = request.sexo,
            peso = request.peso,
            edad = request.edad,
            fechaSalida = request.fechaSalida,
            estado = request.estado
        )
        repository.insert(newAnimal)
    }

    private fun Animal.toResponse() = AnimalResponse(
        idAnimal, nombre, especie, raza, sexo, peso, edad, fechaSalida, estado
    )
}