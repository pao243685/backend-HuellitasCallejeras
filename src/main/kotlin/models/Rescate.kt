package com.example.models

import kotlinx.serialization.Serializable


data class Rescate(
    val idRescate: Int,
    val fechaIngreso: String,
    val lugar: String,
    val descripcion: String,
    val idAnimalito: Int
)
