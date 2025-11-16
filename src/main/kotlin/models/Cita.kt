package com.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDate

data class Cita(
    val idCitas: Int,
    val fechaRealizacion: LocalDate,
    val fechaCita: LocalDate,
    val motivo: String,
    val lugar: String,
    val idAnimalito: Int
)
