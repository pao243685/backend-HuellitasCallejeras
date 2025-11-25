package com.example.domain.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Animalito(
    val id: Int = 0,
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    @Contextual val fechaSalida: Instant? = null
)

@Serializable
data class AnimalitoRequest(
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String
)