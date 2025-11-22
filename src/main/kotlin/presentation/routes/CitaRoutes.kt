package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.CitaRequest
import com.example.domain.models.services.CitaService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID
import kotlin.text.toIntOrNull

fun Route.citaRoutes(service: CitaService) {
    route("/citas") {

        get {
            val citas = service.getAllCitas()
            call.respond(ApiResponse(true, "Citas obtenidas", citas))
        }

        get("/pendientes") {
            val citas = service.getCitasPendientes()
            call.respond(ApiResponse(true, "Citas pendientes", citas))
        }

        get("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val cita = service.getCitaById(id)
            if (cita != null) {
                call.respond(ApiResponse(true, "Cita encontrada", cita))
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Any>(false, "No encontrada")
                )
            }
        }

        get("/animalito/{animalitoId}") {
            val idParam = call.parameters["animalitoId"]
            val animalitoId = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val citas = service.getCitasByAnimalito(animalitoId)
            call.respond(ApiResponse(true, "Citas del animalito", citas))
        }

        post {
            try {
                val request = call.receive<CitaRequest>()
                val cita = service.createCita(request)
                call.respond(HttpStatusCode.Created, ApiResponse(true, "Cita creada", cita))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error")
                )
            }
        }

        put("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val request = call.receive<CitaRequest>()
            val updated = service.updateCita(id, request)

            call.respond(
                if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(updated, if (updated) "Actualizada" else "No encontrada", null)
            )
        }

        delete("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val deleted = service.deleteCita(id)
            call.respond(
                if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(deleted, if (deleted) "Eliminada" else "No encontrada", null)
            )
        }
    }
}