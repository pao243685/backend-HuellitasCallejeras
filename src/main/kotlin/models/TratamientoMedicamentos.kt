package com.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDate

data class TratamientoMedicamentos(
    val idTratamiento: Int,
    val idMedicamento: Int,
    val dosis: Double,
    val fechaConclusion: LocalDate,
    val repeticion: Double
)
