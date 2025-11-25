package com.example.domain.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Tratamiento(
    val id: Int = 0,
    @Contextual val fechaInicio: Instant,
    val receta: String
)

@Serializable
data class TratamientoRequest(
    val receta: String,
    val animalitoId: Int,
    val medicamentos: List<MedicamentoTratamiento>
)