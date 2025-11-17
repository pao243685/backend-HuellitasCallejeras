package com.example.services

import com.example.Routing.request.AnimalRequest
import com.example.Routing.request.CitasRequest
import com.example.repository.CitasRepository
import com.example.Routing.response.CitaResponse
import com.example.models.Cita
import kotlinx.datetime.LocalDate
import java.util.UUID

class CitasService(private val repository: CitasRepository) {

    suspend fun getAll(): List<CitaResponse> =
        repository.getAllCitas().map { it.toResponse() }

    suspend fun addCita(request: CitasRequest){
        val newCita = Cita(
            idCitas = UUID.randomUUID(),
            fechaRealizacion = request.fecha_realizacion,
            fechaCita = request.fecha_cita,
            motivo = request.motivo,
            lugar = request.lugar,
            idAnimalito = request.animal_id,

        )
        repository.insert(newCita)
    }

    private fun Cita.toResponse() = CitaResponse(
        idCitas, fechaRealizacion, fechaCita, motivo, lugar, idAnimalito
    )

}