package com.example.routes

import com.example.controller.AnimalController
import com.example.models.Animal
import com.example.services.AnimalService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.animalRoutes(controller: AnimalController) {

    route("/api") {

        get("/") {
            call.respondText("API funcionando ✔")
        }

        get("/db-test") {
            transaction { exec("SELECT 1") }
            call.respondText("Conexión a BD correcta ✔")
        }

        route("/animal") {
            get { controller.getAll(call) }
            post { controller.add(call) }
        }
    }

}