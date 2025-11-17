package com.example.controller

import com.example.Routing.request.AnimalRequest
import com.example.Routing.request.CitasRequest
import com.example.services.CitasService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class CitaController(private val service: CitasService) {

    suspend fun getAll(call: ApplicationCall) {
        val citas = service.getAll()
        call.respond(citas)
    }

    suspend fun add(call: ApplicationCall) {
        try {
            val request = call.receive<CitasRequest>()
            service.addCita(request)
            call.respond(HttpStatusCode.OK, request)
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        }
    }
}