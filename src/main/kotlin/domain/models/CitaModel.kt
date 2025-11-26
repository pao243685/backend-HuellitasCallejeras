package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Cita(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaRealizacion: Instant,
    @Contextual val fechaCita: Instant,
    val titulo: String,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID
)

@Serializable
data class CitaRequest(
    @Contextual val fechaCita: Instant,
    val titulo: String,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID
)