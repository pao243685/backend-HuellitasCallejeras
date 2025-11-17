package com.example.routes

import com.example.Routing.request.AnimalRequest
import com.example.controller.CitaController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import jdk.internal.vm.ScopedValueContainer.call

fun Route.citasRoutes(controller: CitaController) {

    route("/citas") {
        get{ controller.getAll(call) }
        post { controller.add(call) }
    }
}