package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.TratamientoRequest
import com.example.domain.models.TratamientoRequestSinReceta
import com.example.domain.models.services.S3Service
import com.example.domain.models.services.TratamientoService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.util.UUID
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receiveMultipart
import kotlinx.serialization.json.Json
import java.time.Instant

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
                val multipart = call.receiveMultipart()

                var tratamientoRequest: TratamientoRequestSinReceta? = null
                var archivoBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "tratamiento" -> {
                                    try {
                                        tratamientoRequest = Json.decodeFromString<TratamientoRequestSinReceta>(part.value)
                                    } catch (e: Exception) {
                                        // Error parsing JSON will be handled below
                                    }
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "archivo") {
                                try {
                                    archivoBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "application/octet-stream"
                                } catch (e: Exception) {
                                    // Error reading file will be handled below
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (tratamientoRequest == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos del tratamiento son requeridos")
                    )
                }

                if (archivoBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo de receta es requerido")
                    )
                }

                val uploadResult = S3Service.uploadFile(
                    archivoBytes,
                    contentType!!,
                    "tratamientos"
                )

                if (!uploadResult.success) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error subiendo archivo: ${uploadResult.message}")
                    )
                }

                val tratamientoCompleto = TratamientoRequest(
                    fechaInicio = tratamientoRequest.fechaInicio,
                    receta = uploadResult.url,
                    animalId = tratamientoRequest.animalId,
                    medicamentos = tratamientoRequest.medicamentos
                )

                val tratamiento = service.createTratamiento(tratamientoCompleto)

                if (tratamiento != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(true, "Tratamiento creado", tratamiento)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error creando tratamiento en base de datos")
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error interno del servidor")
                )
            }
        }

        put("/{id}") {
            try {
                val rawId = call.parameters["id"]
                val id = try {
                    UUID.fromString(rawId)
                } catch (e: Exception) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )
                }

                val multipart = call.receiveMultipart()

                var archivoBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (part.name == "archivo") {
                                try {
                                    archivoBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "application/octet-stream"
                                } catch (e: Exception) {
                                    // Error reading file will be handled below
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (archivoBytes == null) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Archivo de receta requerido")
                    )
                }

                // Subir archivo a S3 y obtener URL
                val uploadResult = S3Service.uploadFile(
                    archivoBytes,
                    contentType!!,
                    "tratamientos"
                )

                if (!uploadResult.success) {
                    return@put call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error subiendo archivo: ${uploadResult.message}")
                    )
                }

                // Actualizar tratamiento con la nueva URL de S3 usando el método existente
                val updated = service.updateTratamiento(id, uploadResult.url)

                call.respond(
                    if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                    ApiResponse(updated, if (updated) "Tratamiento actualizado" else "Tratamiento no encontrado", null)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error: ${e.message}")
                )
            }
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