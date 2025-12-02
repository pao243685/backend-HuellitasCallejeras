package com.example.domain.models

import com.example.InstantSerializer
import com.example.UUIDSerializer
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
    @Serializable(with = InstantSerializer::class)
    val fechaSalida: Instant? = null,
    val urlImage: String,
    @Serializable(with = UUIDSerializer::class)
    val rescatistaId: UUID

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
    @Serializable(with = InstantSerializer::class)
    val fechaSalida: Instant? = null,
    val urlImage: String,
    @Serializable(with = UUIDSerializer::class)
    val rescatistaId: UUID
)

@Serializable
data class AnimalRequestsinImagen(
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    @Serializable(with = InstantSerializer::class)
    val fechaSalida: Instant? = null,
    @Serializable(with = UUIDSerializer::class)
    val rescatistaId: UUID
)