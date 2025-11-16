package com.example.models

import kotlinx.serialization.Serializable
import java.util.Date

data class tratamiento(
    val idTratamiento: Int,
    val fechaInicio: Date,
    val receta: String
    )
