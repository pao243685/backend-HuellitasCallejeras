package com.example.routes

import com.example.controller.AnimalController
import com.example.models.Animal
import com.example.services.AnimalService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.animalRoutes(controller: AnimalController) {

    route("/animal") {
        get { controller.getAll(call) }
        post { controller.add(call) }
    }

}