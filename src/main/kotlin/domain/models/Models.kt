package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.Instant
import java.util.UUID

@Serializable
data class Animalito(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    @Contextual val fechaSalida: Instant? = null,
    val urlImage: String
)

@Serializable
data class AnimalitoRequest(
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    val urlImage: String
)

@Serializable
data class Rescate(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaIngreso: Instant,
    val lugar: String,
    val descripcion: String,
    @Serializable(with = UUIDSerializer::class)
    val animalitoId: UUID
)

@Serializable
data class RescateRequest(
    val lugar: String,
    val descripcion: String,
    @Serializable(with = UUIDSerializer::class)
    val animalitoId: UUID
)

@Serializable
data class Tratamiento(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaInicio: Instant,
    val receta: String
)

@Serializable
data class TratamientoRequest(
    val receta: String,
    @Serializable(with = UUIDSerializer::class)
    val animalitoId: UUID,
    val medicamentos: List<MedicamentoTratamiento>
)

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

@Serializable
data class MedicamentoTratamiento(
    @Serializable(with = UUIDSerializer::class)
    val medicamentoId: UUID,
    val dosis: Float,
    val repeticion: Float,
    @Contextual val fechaConclusion: Instant? = null
)

@Serializable
data class Cita(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Contextual val fechaRealizacion: Instant,
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animalitoId: UUID
)

@Serializable
data class CitaRequest(
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    @Serializable(with = UUIDSerializer::class)
    val animalitoId: UUID
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)