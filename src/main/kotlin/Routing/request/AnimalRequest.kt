package com.example.Routing.request


import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
data class AnimalRequest(
    val nombre: String,
    val especie: String,
    val raza: String,
    val sexo: String,
    val peso: Double,
    val edad: Int,
    val fechaSalida: LocalDate? = null,
    val estado: String
)
