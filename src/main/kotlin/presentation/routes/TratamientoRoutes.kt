package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.TratamientoRequest
import com.example.domain.models.services.TratamientoService
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

fun Route.tratamientoRoutes(service: TratamientoService) {
    route("/tratamientos") {

        get {
            val tratamientos = service.getAllTratamientos()
            call.respond(ApiResponse(true, "Tratamientos obtenidos", tratamientos))
        }

        get("/{id}") {
            val rawId = call.parameters["id"]
            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            val tratamiento = service.getTratamientoById(id)
            if (tratamiento != null) {
                call.respond(ApiResponse(true, "Tratamiento encontrado", tratamiento))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Any>(false, "No encontrado"))
            }
        }

        get("/{id}/medicamentos") {
            val rawId = call.parameters["id"]
            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            val medicamentos = service.getMedicamentosByTratamiento(id)
            call.respond(ApiResponse(true, "Medicamentos obtenidos", medicamentos))
        }

        get("/animal/{animalId}") {
            val rawId = call.parameters["animalId"]
            val animalId = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            val tratamientos = service.getTratamientosByAnimal(animalId)
            call.respond(ApiResponse(true, "Tratamientos del animal", tratamientos))
        }

        post {
            try {
                val request = call.receive<TratamientoRequest>()
                val tratamiento = service.createTratamiento(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(true, "Tratamiento creado", tratamiento)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error")
                )
            }
        }

        put("/{id}") {
            val rawId = call.parameters["id"]
            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            val receta = call.receive<Map<String, String>>()["receta"] ?: return@put call.respond(
                HttpStatusCode.BadRequest, ApiResponse<Any>(false, "Receta requerida")
            )

            val updated = service.updateTratamiento(id, receta)

            call.respond(
                if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(updated, if (updated) "Actualizado" else "No encontrado", null)
            )
        }

        delete("/{id}") {
            val rawId = call.parameters["id"]
            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            val deleted = service.deleteTratamiento(id)

            call.respond(
                if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(deleted, if (deleted) "Eliminado" else "No encontrado", null)
            )
        }
    }
}
