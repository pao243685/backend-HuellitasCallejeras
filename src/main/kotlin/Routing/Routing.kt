package com.example.Routing

import com.example.controller.AnimalController
import com.example.repository.AnimalRepositorio
import com.example.routes.animalRoutes
import com.example.services.AnimalService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        val repository = AnimalRepositorio()
        val service = AnimalService(repository)
        val controller = AnimalController(service)
        animalRoutes(controller)
    }
}