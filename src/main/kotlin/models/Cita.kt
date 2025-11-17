package com.example.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class Cita(
    val idCitas: UUID,
    val fechaRealizacion: LocalDateTime,
    val fechaCita: LocalDateTime,
    val motivo: String,
    val lugar: String,
    val idAnimalito: UUID
)
