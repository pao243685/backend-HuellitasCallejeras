package com.example.Routing

import com.example.controller.AnimalController
import com.example.controller.CitaController
import com.example.repository.AnimalRepositorio
import com.example.repository.CitasRepository
import com.example.routes.animalRoutes
import com.example.routes.citasRoutes
import com.example.services.AnimalService
import com.example.services.CitasService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        val repository = AnimalRepositorio()
        val repositoryCitas = CitasRepository()

        val service = AnimalService(repository)
        val serviceCita = CitasService(repositoryCitas)

        val controller = AnimalController(service)
        val controllerCita = CitaController(serviceCita)

        animalRoutes(controller)
        citasRoutes(controllerCita)
    }
}