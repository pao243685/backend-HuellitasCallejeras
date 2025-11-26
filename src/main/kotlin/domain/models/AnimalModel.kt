package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Animal(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    @Contextual val fechaSalida: Instant? = null,
    val urlImage: String
)

@Serializable
data class AnimalRequest(
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    val urlImage: String
)