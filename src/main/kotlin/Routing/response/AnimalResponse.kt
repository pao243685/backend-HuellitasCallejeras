package com.example.Routing.response

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import java.util.UUID


@Serializable
data class AnimalResponse(
    val id: UUID,
    val nombre: String,
    val especie: String,
    val raza: String,
    val sexo: String,
    val peso: Double,
    val edad: Int,
    val fechaSalida: LocalDate? = null,
    val estado: String
)

