package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Tratamiento(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaInicio: Instant,
    val receta: String
)

@Serializable
data class TratamientoRequest(
    val receta: String,
    @Serializable(with = UUIDSerializer::class)
    val animalId: UUID,
    val medicamentos: List<MedicamentoTratamiento>
)