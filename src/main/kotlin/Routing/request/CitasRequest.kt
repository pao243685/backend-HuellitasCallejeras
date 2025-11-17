package com.example.Routing.request

import com.example.models.Cita
import com.example.util.UUIDSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CitasRequest(
    val fecha_realizacion: LocalDate? = null,
    val fecha_cita: LocalDate? = null,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animal_id: UUID
)