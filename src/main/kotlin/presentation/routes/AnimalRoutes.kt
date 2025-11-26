package com.example.presentation.routes

import com.example.domain.models.AnimalRequest
import com.example.domain.models.AnimalRescateRequest
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

    route("/animal") {

        get {
            try {
                val animal = service.getAllAnimal()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Animal obtenidos exitosamente",
                        data = animal
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error al obtener animal: ${e.message}"
                    )
                )
            }
        }

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

                val animal = service.getAnimalById(id)
                if (animal != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animal encontrado",
                            data = animal
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animal no encontrado"
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

        get("/estado/{estado}") {
            try {
                val estado = call.parameters["estado"] ?: ""
                val animales = service.getAnimalByEstado(estado)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Animales filtrados por estado",
                        data = animales
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

        post {
            try {
                val request = call.receive<AnimalRequest>()
                val animal = service.createAnimal(request)

                if (animal != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            message = "Animal creado exitosamente",
                            data = animal
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error al crear animal"
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

                val request = call.receive<AnimalRequest>()
                val updated = service.updateAnimal(id, request)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animal actualizado exitosamente",
                            data = null
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animal no encontrado"
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

                val deleted = service.deleteAnimal(id)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Animal eliminado exitosamente",
                            data = null
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animal no encontrado"
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

                post("/crear-con-rescate") {
                    try {
                        val request = call.receive<AnimalRescateRequest>()
                        val resultado = service.createAnimalConRescate(request.animal, request.rescate)

                        if (resultado != null) {
                            call.respond(
                                HttpStatusCode.Created,
                                ApiResponse(
                                    success = true,
                                    message = "Animal y rescate creados exitosamente",
                                    data = resultado
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ApiResponse<Any>(
                                    success = false,
                                    message = "Error al crear animal y rescate"
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

                put("/{id}/actualizar-con-rescate") {
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

                        val request = call.receive<AnimalRescateRequest>()
                        val resultado = service.updateAnimalConRescate(id, request.animal, request.rescate)
                        if (resultado != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                ApiResponse(
                                    success = true,
                                    message = "Animal y rescate actualizados exitosamente",
                                    data = resultado
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ApiResponse<Any>(
                                    success = false,
                                    message = "Animal no encontrado"
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

                get("/{id}/con-rescate") {
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
                            return@get
                        }

                        val resultado = service.getAnimalConRescate(id)
                        if (resultado != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                ApiResponse(
                                    success = true,
                                    message = "Animal y rescate encontrados",
                                    data = resultado
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ApiResponse<Any>(
                                    success = false,
                                    message = "Animal no encontrado"
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