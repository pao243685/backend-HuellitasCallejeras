package com.example.domain.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class MedicamentoTratamiento(
    val medicamentoId: Int,
    val dosis: Float,
    val repeticion: Float,
    @Contextual val fechaConclusion: Instant? = null
)
