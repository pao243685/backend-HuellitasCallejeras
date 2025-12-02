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
                println("🔵 [DEBUG] Iniciando endpoint POST /tratamientos")
                val multipart = call.receiveMultipart()
                println("🔵 [DEBUG] Multipart recibido correctamente")

                var tratamientoRequest: TratamientoRequestSinReceta? = null
                var archivoBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            println("🔵 [DEBUG] Procesando FormItem: ${part.name} = ${part.value.take(100)}...")
                            when (part.name) {
                                "tratamiento" -> {
                                    try {
                                        println("🔵 [DEBUG] Intentando parsear JSON del tratamiento...")
                                        tratamientoRequest = Json.decodeFromString<TratamientoRequestSinReceta>(part.value)
                                        println("🔵 [DEBUG] JSON parseado exitosamente: $tratamientoRequest")
                                    } catch (e: Exception) {
                                        println("🔴 [ERROR] Error parseando JSON: ${e.message}")
                                        println("🔴 [ERROR] JSON recibido: ${part.value}")
                                        e.printStackTrace()
                                    }
                                }
                                else -> {
                                    println("🟡 [WARN] FormItem desconocido: ${part.name}")
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            println("🔵 [DEBUG] Procesando FileItem: ${part.name}, filename: ${part.originalFileName}, contentType: ${part.contentType}")
                            if (part.name == "archivo") {
                                try {
                                    archivoBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "application/octet-stream"
                                    println("🔵 [DEBUG] Archivo leído: ${archivoBytes?.size} bytes, contentType: $contentType")
                                } catch (e: Exception) {
                                    println("🔴 [ERROR] Error leyendo archivo: ${e.message}")
                                    e.printStackTrace()
                                }
                            } else {
                                println("🟡 [WARN] FileItem desconocido: ${part.name}")
                            }
                        }
                        else -> {
                            println("🟡 [WARN] Tipo de part desconocido: ${part::class.simpleName}")
                        }
                    }
                    part.dispose()
                }

                println("🔵 [DEBUG] Validando datos recibidos...")
                println("🔵 [DEBUG] tratamientoRequest: $tratamientoRequest")
                println("🔵 [DEBUG] archivoBytes size: ${archivoBytes?.size}")
                println("🔵 [DEBUG] contentType: $contentType")

                if (tratamientoRequest == null) {
                    println("🔴 [ERROR] tratamientoRequest es null")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos del tratamiento son requeridos")
                    )
                }

                if (archivoBytes == null) {
                    println("🔴 [ERROR] archivoBytes es null")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo de receta es requerido")
                    )
                }

                println("🔵 [DEBUG] Subiendo archivo a S3...")
                val uploadResult = S3Service.uploadFile(
                    archivoBytes,
                    contentType!!,
                    "tratamientos"
                )

                println("🔵 [DEBUG] Resultado de S3: ${uploadResult.success}, mensaje: ${uploadResult.message}, url: ${uploadResult.url}")

                if (!uploadResult.success) {
                    println("🔴 [ERROR] Error subiendo archivo a S3: ${uploadResult.message}")
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error subiendo archivo: ${uploadResult.message}")
                    )
                }

                println("🔵 [DEBUG] Creando tratamiento completo...")
                val tratamientoCompleto = TratamientoRequest(
                    fechaInicio = tratamientoRequest.fechaInicio,
                    receta = uploadResult.url,
                    animalId = tratamientoRequest.animalId,
                    medicamentos = tratamientoRequest.medicamentos
                )

                println("🔵 [DEBUG] TratamientoRequest completo: $tratamientoCompleto")
                println("🔵 [DEBUG] Llamando a service.createTratamiento...")

                val tratamiento = service.createTratamiento(tratamientoCompleto)

                if (tratamiento != null) {
                    println("🟢 [SUCCESS] Tratamiento creado exitosamente: $tratamiento")
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(true, "Tratamiento creado", tratamiento)
                    )
                } else {
                    println("🔴 [ERROR] service.createTratamiento retornó null")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error creando tratamiento en base de datos")
                    )
                }

            } catch (e: Exception) {
                println("🔴 [ERROR] Excepción general en endpoint: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error interno del servidor")
                )
            }
        }

        put("/{id}") {
            try {
                println("🔵 [DEBUG] Iniciando endpoint PUT /tratamientos/{id}")

                val rawId = call.parameters["id"]
                val id = try {
                    UUID.fromString(rawId)
                } catch (e: Exception) {
                    println("🔴 [ERROR] ID inválido: $rawId")
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )
                }

                println("🔵 [DEBUG] ID del tratamiento a actualizar: $id")
                val multipart = call.receiveMultipart()
                println("🔵 [DEBUG] Multipart recibido correctamente")

                var tratamientoRequest: TratamientoRequestSinReceta? = null
                var archivoBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            println("🔵 [DEBUG] Procesando FormItem: ${part.name}")

                            if (part.name == "tratamiento") {
                                try {
                                    tratamientoRequest = Json.decodeFromString(
                                        TratamientoRequestSinReceta.serializer(),
                                        part.value
                                    )
                                    println("🔵 [DEBUG] JSON parseado: $tratamientoRequest")
                                } catch (e: Exception) {
                                    println("🔴 [ERROR] Error parseando JSON: ${e.message}")
                                    return@forEachPart
                                }
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "archivo") {
                                println("🔵 [DEBUG] Archivo recibido: ${part.originalFileName}")

                                archivoBytes = part.streamProvider().readBytes()
                                contentType = part.contentType?.toString()
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                // Validación de datos básicos
                if (tratamientoRequest == null) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos del tratamiento son requeridos")
                    )
                }

                // Obtener tratamiento existente
                val tratamientoExistente = service.getTratamientoById(id)
                if (tratamientoExistente == null) {
                    return@put call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(false, "Tratamiento no encontrado")
                    )
                }

                // Manejar receta previa o nueva
                var recetaFinal = tratamientoExistente.receta

                if (archivoBytes != null) {
                    println("🔵 [DEBUG] Subiendo archivo nuevo a S3...")

                    val uploadResult = S3Service.uploadFile(
                        archivoBytes,
                        contentType ?: "application/octet-stream",
                        "tratamientos"
                    )

                    if (!uploadResult.success) {
                        return@put call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse<Any>(false, "Error subiendo archivo: ${uploadResult.message}")
                        )
                    }

                    recetaFinal = uploadResult.url!!
                }

                // Crear request final con receta previa o nueva
                val tratamientoCompleto = TratamientoRequest(
                    fechaInicio = tratamientoRequest!!.fechaInicio,
                    receta = recetaFinal,
                    animalId = tratamientoRequest!!.animalId,
                    medicamentos = tratamientoRequest!!.medicamentos
                )

                println("🔵 [DEBUG] TratamientoRequest final: $tratamientoCompleto")

                val updated = service.updateTratamiento(id, tratamientoCompleto)

                if (updated) {
                    println("🟢 [SUCCESS] Tratamiento actualizado exitosamente")
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "Tratamiento actualizado", null)
                    )
                } else {
                    println("🔴 [ERROR] No se encontró el tratamiento")
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(false, "Tratamiento no encontrado")
                    )
                }

            } catch (e: Exception) {
                println("🔴 [ERROR] Excepción general en PUT: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error interno del servidor")
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