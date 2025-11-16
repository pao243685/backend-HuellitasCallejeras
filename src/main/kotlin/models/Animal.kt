package com.example.models

import kotlinx.serialization.Serializable
import java.sql.Date
import kotlinx.datetime.LocalDate
import java.util.UUID


data class Animal(
    val idAnimal: UUID,
    val nombre: String,
    val raza: String,
    val edad: Int,
    val peso: Double,
    val especie: String,
    val fechaSalida: LocalDate? = null,
    val estado: String,
    val sexo: String
    )

