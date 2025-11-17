package com.example.Routing.response

import com.example.util.UUIDSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CitaResponse(
    @Serializable(with = UUIDSerializer::class)
    val cita_id: UUID,
    val fecha_realizacion: LocalDate? = null,
    val fecha_cita: LocalDate? = null,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animal_id: UUID
)