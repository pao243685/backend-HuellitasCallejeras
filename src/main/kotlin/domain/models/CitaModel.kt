package com.example.domain.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Cita(
    val id: Int = 0,
    @Contextual val fechaRealizacion: Instant,
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    val animalitoId: Int
)

@Serializable
data class CitaRequest(
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    val animalitoId: Int
)
