package com.example.controller

import com.example.Routing.request.AnimalRequest
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
        val request = call.receive<AnimalRequest>()
        service.addCita(request)
        call.respond(HttpStatusCode.OK)
    }
}