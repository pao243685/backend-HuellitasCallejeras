package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Medicamento(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val nombre: String
)

@Serializable
data class MedicamentoRequest(
    val nombre: String
)