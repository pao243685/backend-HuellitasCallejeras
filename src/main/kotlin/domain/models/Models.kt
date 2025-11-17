package com.example.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.Instant

@Serializable
data class Animalito(
    val id: Int = 0,
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String,
    @Contextual val fechaSalida: Instant? = null
)

@Serializable
data class AnimalitoRequest(
    val nombre: String,
    val peso: Float,
    val raza: String? = null,
    val sexo: String,
    val edad: Int,
    val especie: String,
    val estado: String
)

@Serializable
data class Rescate(
    val id: Int = 0,
    @Contextual val fechaIngreso: Instant,
    val lugar: String,
    val descripcion: String,
    val animalitoId: Int
)

@Serializable
data class RescateRequest(
    val lugar: String,
    val descripcion: String,
    val animalitoId: Int
)

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

@Serializable
data class Medicamento(
    val id: Int = 0,
    val nombre: String
)

@Serializable
data class MedicamentoRequest(
    val nombre: String
)

@Serializable
data class MedicamentoTratamiento(
    val medicamentoId: Int,
    val dosis: Float,
    val repeticion: Float,
    @Contextual val fechaConclusion: Instant? = null
)

@Serializable
data class Cita(
    val id: Int = 0,
    @Contextual val fechaRealizacion: Instant,
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    val animalitoId: Int
)

@Serializable
data class CitaRequest(
    @Contextual val fechaCita: Instant,
    val motivo: String,
    val lugar: String,
    val animalitoId: Int
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)