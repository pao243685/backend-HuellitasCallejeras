package com.example.controller

import com.example.Routing.request.AnimalRequest
import com.example.models.Animal
import com.example.services.AnimalService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class AnimalController(private val service: AnimalService) {

    suspend fun getAll(call: ApplicationCall) {
        val animals = service.getAll()
        call.respond(animals)
    }

    suspend fun add(call: ApplicationCall) {
        val request = call.receive<AnimalRequest>()
        service.add(request)
        call.respond(HttpStatusCode.Created)
    }
}