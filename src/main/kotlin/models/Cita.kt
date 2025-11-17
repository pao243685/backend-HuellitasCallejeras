package com.example.models

import kotlinx.datetime.LocalDate
import java.util.UUID

data class Cita(
    val idCitas: UUID,
    val fechaRealizacion: LocalDate,
    val fechaCita: LocalDate,
    val motivo: String,
    val lugar: String,
    val idAnimalito: UUID
)
