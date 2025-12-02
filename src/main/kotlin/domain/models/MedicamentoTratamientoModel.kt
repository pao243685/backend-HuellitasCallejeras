package com.example.domain.models

import com.example.InstantSerializer
import com.example.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class MedicamentoTratamiento(
    @Serializable(with = UUIDSerializer::class)
    val medicamentoId: UUID,
    val nombre: String,
    val dosis: Float,
    val repeticion: Float,
    @Serializable(with = InstantSerializer::class)
    val fechaConclusion: Instant? = null
)