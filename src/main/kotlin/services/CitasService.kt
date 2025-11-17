package com.example.services

import com.example.Routing.request.AnimalRequest
import com.example.repository.CitasRepository
import com.example.Routing.response.CitaResponse
import com.example.models.Cita
import kotlinx.datetime.LocalDate
import java.util.UUID

class CitasService(private val repository: CitasRepository) {

    suspend fun getAll(): List<CitaResponse> =
        repository.getAllCitas().map { it.toResponse() }

    suspend fun addCita(request: AnimalRequest){
        val newCita = Cita(
            idCitas = UUID.randomUUID(),
            fechaRealizacion = request.fecha_realizacion as LocalDate,
            fechaCita = request.fecha_cita as LocalDate,
            motivo = request.motivo,
            lugar = request.lugar,
            idAnimalito = request.animal_id,

        )
    }

    private fun Cita.toResponse() = CitaResponse(
        idCitas, fechaRealizacion, fechaCita, motivo, lugar, idAnimalito
    )

}