package com.example.domain.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Rescate(
    val id: Int = 0,
    @Contextual val fechaIngreso: Instant,
    val lugar: String,
    val descripcion: String,
    val animalitoId: Int
)

@Serializable
data class RescateRequest(
    val lugar: String,
    val descripcion: String,
    val animalitoId: Int
)