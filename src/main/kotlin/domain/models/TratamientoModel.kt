package com.example.domain.models

import com.example.InstantSerializer
import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Tratamiento(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = InstantSerializer::class)
    val fechaInicio: Instant,
    val receta: String
)

@Serializable
data class TratamientoRequest(
    @Serializable(with = InstantSerializer::class)
    val fechaInicio: Instant,
    val receta: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID,
    val medicamentos: List<MedicamentoTratamiento>
)

@Serializable
data class TratamientoRequestSinReceta(
    @Serializable(with = InstantSerializer::class)
    val fechaInicio: Instant,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID,
    val medicamentos: List<MedicamentoTratamiento>
)