package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.RescateRequest
import com.example.domain.models.services.RescateService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.text.toIntOrNull


fun Route.rescateRoutes(service: RescateService) {
    route("/rescates") {
        get {
            val rescates = service.getAllRescates()
            call.respond(ApiResponse(true, "Rescates obtenidos", rescates))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido")
            )
            val rescate = service.getRescateById(id)
            if (rescate != null) {
                call.respond(ApiResponse(true, "Rescate encontrado", rescate))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Any>(false, "No encontrado"))
            }
        }

        get("/animalito/{animalitoId}") {
            val animalitoId = call.parameters["animalitoId"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido")
            )
            val rescates = service.getRescatesByAnimalito(animalitoId)
            call.respond(ApiResponse(true, "Rescates del animalito", rescates))
        }

        post {
            try {
                val request = call.receive<RescateRequest>()
                val rescate = service.createRescate(request)
                call.respond(HttpStatusCode.Created, ApiResponse(true, "Rescate creado", rescate))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Any>(false, e.message ?: "Error"))
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido")
            )
            val request = call.receive<RescateRequest>()
            val updated = service.updateRescate(id, request)
            call.respond(if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(updated, if (updated) "Actualizado" else "No encontrado", null))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido")
            )
            val deleted = service.deleteRescate(id)
            call.respond(if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(deleted, if (deleted) "Eliminado" else "No encontrado", null))
        }
    }
}