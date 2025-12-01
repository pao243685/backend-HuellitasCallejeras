package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.MedicamentoTratamientoRequest
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
import kotlin.toString

fun Route.tratamientoRoutes(service: TratamientoService) {
    route("/tratamientos") {

        get {
            println("DEBUG: GET /tratamientos - Obteniendo todos los tratamientos")
            val tratamientos = service.getAllTratamientos()
            println("DEBUG: GET /tratamientos - Tratamientos obtenidos: ${tratamientos.size} elementos")
            call.respond(ApiResponse(true, "Tratamientos obtenidos", tratamientos))
        }

        get("/{id}") {
            val rawId = call.parameters["id"]
            println("DEBUG: GET /tratamientos/{id} - ID recibido: $rawId")

            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: GET /tratamientos/{id} - Error al convertir ID: ${e.message}")
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            println("DEBUG: GET /tratamientos/{id} - Buscando tratamiento con ID: $id")
            val tratamiento = service.getTratamientoById(id)
            if (tratamiento != null) {
                println("DEBUG: GET /tratamientos/{id} - Tratamiento encontrado: $tratamiento")
                call.respond(ApiResponse(true, "Tratamiento encontrado", tratamiento))
            } else {
                println("DEBUG: GET /tratamientos/{id} - Tratamiento no encontrado para ID: $id")
                call.respond(HttpStatusCode.NotFound, ApiResponse<Any>(false, "No encontrado"))
            }
        }

        get("/{id}/medicamentos") {
            val rawId = call.parameters["id"]
            println("DEBUG: GET /tratamientos/{id}/medicamentos - ID recibido: $rawId")

            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: GET /tratamientos/{id}/medicamentos - Error al convertir ID: ${e.message}")
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            println("DEBUG: GET /tratamientos/{id}/medicamentos - Buscando medicamentos para tratamiento: $id")
            val medicamentos = service.getMedicamentosByTratamiento(id)
            println("DEBUG: GET /tratamientos/{id}/medicamentos - Medicamentos obtenidos: ${medicamentos.size} elementos")
            call.respond(ApiResponse(true, "Medicamentos obtenidos", medicamentos))
        }

        get("/animal/{animalId}") {
            val rawId = call.parameters["animalId"]
            println("DEBUG: GET /tratamientos/animal/{animalId} - Animal ID recibido: $rawId")

            val animalId = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: GET /tratamientos/animal/{animalId} - Error al convertir Animal ID: ${e.message}")
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            println("DEBUG: GET /tratamientos/animal/{animalId} - Buscando tratamientos para animal: $animalId")
            val tratamientos = service.getTratamientosByAnimal(animalId)
            println("DEBUG: GET /tratamientos/animal/{animalId} - Tratamientos obtenidos: ${tratamientos.size} elementos")
            call.respond(ApiResponse(true, "Tratamientos del animal", tratamientos))
        }

        post {
            println("DEBUG: POST /tratamientos - Iniciando creación de tratamiento")
            try {
                val multipart = call.receiveMultipart()
                println("DEBUG: POST /tratamientos - Multipart recibido")

                var tratamientoRequest: TratamientoRequestSinReceta? = null
                var archivoBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    println("DEBUG: POST /tratamientos - Procesando parte: ${part.name} de tipo ${part::class.simpleName}")
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "tratamiento") {
                                try {
                                    println("DEBUG: POST /tratamientos - Procesando JSON del tratamiento")
                                    tratamientoRequest = Json.decodeFromString<TratamientoRequestSinReceta>(part.value)
                                    println("DEBUG: POST /tratamientos - JSON decodificado: $tratamientoRequest")
                                } catch (e: Exception) {
                                    println("DEBUG: POST /tratamientos - Error decodificando JSON: ${e.message}")
                                    return@forEachPart call.respond(
                                        HttpStatusCode.BadRequest,
                                        ApiResponse<Any>(false, "JSON inválido en 'tratamiento': ${e.message}")
                                    )
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "archivo") {
                                try {
                                    println("DEBUG: POST /tratamientos - Procesando archivo")
                                    archivoBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "application/octet-stream"
                                    println("DEBUG: POST /tratamientos - Archivo leído: ${archivoBytes!!.size} bytes, tipo: $contentType")
                                } catch (e: Exception) {
                                    println("DEBUG: POST /tratamientos - Error leyendo archivo: ${e.message}")
                                    return@forEachPart call.respond(
                                        HttpStatusCode.BadRequest,
                                        ApiResponse<Any>(false, "Error leyendo archivo: ${e.message}")
                                    )
                                }
                            }
                        }
                        else -> {
                            println("DEBUG: POST /tratamientos - Parte ignorada: ${part.name}")
                        }
                    }
                    part.dispose()
                }

                if (tratamientoRequest == null) {
                    println("DEBUG: POST /tratamientos - Error: tratamientoRequest es nulo")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos del tratamiento son requeridos")
                    )
                }

                if (archivoBytes == null) {
                    println("DEBUG: POST /tratamientos - Error: archivoBytes es nulo")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo de receta es requerido")
                    )
                }

                println("DEBUG: POST /tratamientos - Subiendo archivo a S3")
                val uploadResult = S3Service.uploadFile(
                    archivoBytes,
                    contentType!!,
                    "tratamientos"
                )

                if (!uploadResult.success) {
                    println("DEBUG: POST /tratamientos - Error subiendo archivo: ${uploadResult.message}")
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error subiendo archivo: ${uploadResult.message}")
                    )
                }
                println("DEBUG: POST /tratamientos - Archivo subido exitosamente: ${uploadResult.url}")

                val tratamientoCompleto = TratamientoRequest(
                    fechaInicio = tratamientoRequest.fechaInicio,
                    receta = uploadResult.url,
                    animalId = tratamientoRequest.animalId,
                    medicamentos = tratamientoRequest.medicamentos
                )
                println("DEBUG: POST /tratamientos - Creando tratamiento completo: $tratamientoCompleto")

                val tratamiento = service.createTratamiento(tratamientoCompleto)

                if (tratamiento != null) {
                    println("DEBUG: POST /tratamientos - Tratamiento creado exitosamente: $tratamiento")
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(true, "Tratamiento creado", tratamiento)
                    )
                } else {
                    println("DEBUG: POST /tratamientos - Error creando tratamiento en base de datos")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error creando tratamiento en base de datos")
                    )
                }

            } catch (e: Exception) {
                println("DEBUG: POST /tratamientos - Excepción general: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error interno del servidor")
                )
            }
        }

        put("/{id}") {
            val rawId = call.parameters["id"]
            println("DEBUG: PUT /tratamientos/{id} - ID recibido: $rawId")

            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: PUT /tratamientos/{id} - Error al convertir ID: ${e.message}")
                return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido"))
            }

            println("DEBUG: PUT /tratamientos/{id} - Recibiendo cuerpo de la petición")
            val body = call.receive<Map<String, String>>()
            println("DEBUG: PUT /tratamientos/{id} - Cuerpo recibido: $body")

            val receta = body["receta"]

            if (receta == null) {
                println("DEBUG: PUT /tratamientos/{id} - Error: campo 'receta' no encontrado")
                return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Any>(false, "El campo 'receta' es requerido"))
            }

            println("DEBUG: PUT /tratamientos/{id} - Actualizando tratamiento con ID: $id")
            val updated = service.updateTratamiento(id, receta)
            println("DEBUG: PUT /tratamientos/{id} - Resultado de actualización: $updated")

            call.respond(
                if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(updated, if (updated) "Actualizado" else "No encontrado",null)
            )
        }

        put("/{id}/medicamentos") {
            val rawId = call.parameters["id"]
            println("DEBUG: PUT /tratamientos/{id}/medicamentos - ID recibido: $rawId")

            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: PUT /tratamientos/{id}/medicamentos - Error al convertir ID: ${e.message}")
                return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Any>(false, "ID inválido"))
            }

            println("DEBUG: PUT /tratamientos/{id}/medicamentos - Recibiendo cuerpo de la petición")
            val body = call.receive<MedicamentoTratamientoRequest>()
            println("DEBUG: PUT /tratamientos/{id}/medicamentos - Cuerpo recibido: $body")

            println("DEBUG: PUT /tratamientos/{id}/medicamentos - Actualizando medicamentos para tratamiento: $id")
            val updated = service.updateMedicamentos(id, body.medicamentos)
            println("DEBUG: PUT /tratamientos/{id}/medicamentos - Resultado de actualización: $updated")

            call.respond(
                if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(updated, if (updated) "Medicamentos actualizados" else "Tratamiento no encontrado",null)
            )
        }

        delete("/{id}") {
            val rawId = call.parameters["id"]
            println("DEBUG: DELETE /tratamientos/{id} - ID recibido: $rawId")

            val id = try {
                UUID.fromString(rawId)
            } catch (e: Exception) {
                println("DEBUG: DELETE /tratamientos/{id} - Error al convertir ID: ${e.message}")
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido")
                )
            }

            println("DEBUG: DELETE /tratamientos/{id} - Eliminando tratamiento con ID: $id")
            val deleted = service.deleteTratamiento(id)
            println("DEBUG: DELETE /tratamientos/{id} - Resultado de eliminación: $deleted")

            call.respond(
                if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(deleted, if (deleted) "Eliminado" else "No encontrado", null)
            )
        }
    }
}