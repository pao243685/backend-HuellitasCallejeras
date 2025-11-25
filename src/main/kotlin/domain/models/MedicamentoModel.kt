package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Medicamento(
    val id: Int = 0,
    val nombre: String
)

@Serializable
data class MedicamentoRequest(
    val nombre: String
)
