package com.example.presentation.routes

import com.example.domain.models.AnimalitoRequest
import com.example.domain.models.ApiResponse
import com.example.domain.models.services.AnimalService
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

fun Route.animalRoutes(service: AnimalService) {

    route("/animalitos") {

        // GET /api/animalitos - Obtener todos los animalitos
        get {
            try {
                val animalitos = service.getAllAnimalitos()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Animalitos obtenidos exitosamente",
                        data = animalitos
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error al obtener animalitos: ${e.message}"
                    )
                )
            }
        }

        // GET /api/animalitos/{id} - Obtener un animalito por ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.let { UUID.fromString(it)}
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "ID inválido"
                        )
                    )
                    return@get
                }

                val animalito = service.getAnimalitoById(id)
                if (animalito != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animalito encontrado",
                            data = animalito
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animalito no encontrado"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        // GET /api/animalitos/estado/{estado} - Filtrar por estado
        get("/estado/{estado}") {
            try {
                val estado = call.parameters["estado"] ?: ""
                val animalitos = service.getAnimalitosByEstado(estado)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Animalitos filtrados por estado",
                        data = animalitos
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        // POST /api/animalitos - Crear nuevo animalito
        post {
            try {
                val request = call.receive<AnimalitoRequest>()
                val animalito = service.createAnimalito(request)

                if (animalito != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            message = "Animalito creado exitosamente",
                            data = animalito
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error al crear animalito"
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(
                        success = false,
                        message = "Datos inválidos: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        // PUT /api/animalitos/{id} - Actualizar animalito
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "ID inválido"
                        )
                    )
                    return@put
                }

                val request = call.receive<AnimalitoRequest>()
                val updated = service.updateAnimalito(id, request)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animalito actualizado exitosamente",
                            data = null
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animalito no encontrado"
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(
                        success = false,
                        message = "Datos inválidos: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        // DELETE /api/animalitos/{id} - Eliminar animalito
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "ID inválido"
                        )
                    )
                    return@delete
                }

                val deleted = service.deleteAnimalito(id)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animalito eliminado exitosamente",
                            data = null
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animalito no encontrado"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }
    }
}