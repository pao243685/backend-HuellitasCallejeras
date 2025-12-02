package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RescateRequestSinAnimalId(
    val lugar: String,
    val descripcion: String

)

@Serializable
data class AnimalRescateResponse(
    val animal: Animal,
    val rescate: Rescate
)

