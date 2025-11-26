package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Rescate(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaIngreso: Instant,
    val lugar: String,
    val descripcion: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID
)

@Serializable
data class RescateRequest(
    val lugar: String,
    val descripcion: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID
)
