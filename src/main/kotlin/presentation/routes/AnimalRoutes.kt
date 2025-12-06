package com.example.presentation.routes

import com.example.domain.models.AnimalRequest
import com.example.domain.models.AnimalRequestsinImagen
import com.example.domain.models.ApiResponse
import com.example.domain.models.RescateRequestSinAnimalId
import com.example.domain.models.services.AnimalService
import com.example.domain.models.services.S3Service
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.util.UUID
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receiveMultipart
import kotlinx.serialization.json.Json

fun Route.animalRoutes(service: AnimalService) {

    suspend fun deleteImageFromS3(fileName: String): Boolean {
        return try {
            S3Service.deleteImage(fileName)
        } catch (e: Exception) {
            false
        }
    }

    fun extractFileNameFromS3Url(url: String): String {
        return S3Service.extractFileNameFromS3Url(url)
    }

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

                val animal = service.getAnimalById(id)
                if (animal == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animal no encontrado"
                        )
                    )
                    return@delete
                }

                val deleted = service.deleteAnimal(id)

                if (deleted) {
                    if (animal.urlImage.isNotBlank() && animal.urlImage.contains("amazonaws.com")) {
                        try {
                            val fileName = extractFileNameFromS3Url(animal.urlImage)
                            deleteImageFromS3(fileName)
                        } catch (e: Exception) {
                            // Silently fail on image deletion - animal is already deleted
                        }
                    }

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
                println("DEBUG /crear-con-rescate: Inicio de solicitud multipart")

                val multipart = call.receiveMultipart()

                var animalRequestsinImagen: AnimalRequestsinImagen? = null
                var rescateRequest: RescateRequestSinAnimalId? = null
                var imageBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    println("DEBUG Multipart Part: name=${part.name}, type=${part::class.simpleName}")

                    when (part) {
                        is PartData.FormItem -> {
                            println("DEBUG FormItem recibido: ${part.name}")

                            when (part.name) {
                                "animal" -> {
                                    println("DEBUG Decodificando JSON de animal")
                                    animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                    println("DEBUG AnimalRequestsinImagen: $animalRequestsinImagen")
                                }

                                "rescate" -> {
                                    println("DEBUG Decodificando JSON de rescate")
                                    rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                    println("DEBUG RescateRequest: $rescateRequest")
                                }

                                else -> {
                                    println("DEBUG FormItem desconocido ignorado: ${part.name}")
                                }
                            }
                        }

                        is PartData.FileItem -> {
                            println("DEBUG FileItem recibido: ${part.name}")

                            if (part.name == "imagen") {
                                println("DEBUG Leyendo bytes de imagen...")

                                val bytes = part.streamProvider().readBytes()
                                println("DEBUG Tamaño imagen: ${bytes.size} bytes")

                                imageBytes = bytes
                                contentType = part.contentType?.toString() ?: "image/jpeg"

                                println("DEBUG Content-Type imagen: $contentType")
                            } else {
                                println("DEBUG FileItem desconocido: ${part.name}")
                            }
                        }

                        else -> {
                            println("DEBUG Parte desconocida ignorada")
                        }
                    }

                    part.dispose()
                }

                // Validaciones con debug

                if (animalRequestsinImagen == null) {
                    println("DEBUG Error: animalRequestsinImagen es null")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos de animal son requeridos")
                    )
                }

                if (rescateRequest == null) {
                    println("DEBUG Error: rescateRequest es null")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Datos de rescate son requeridos")
                    )
                }

                if (imageBytes == null) {
                    println("DEBUG Error: imageBytes es null")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "La imagen es requerida")
                    )
                }

                println("DEBUG Llamando a S3Service.uploadAnimalImage...")

                val uploadResult = S3Service.uploadAnimalImage(imageBytes!!, contentType!!)

                println("DEBUG Resultado upload S3: success=${uploadResult.success}, url=${uploadResult.url}, message=${uploadResult.message}")

                if (!uploadResult.success) {
                    println("DEBUG Error subiendo imagen a S3")
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error subiendo imagen: ${uploadResult.message}")
                    )
                }

                val animalRequest = AnimalRequest(
                    nombre = animalRequestsinImagen.nombre,
                    peso = animalRequestsinImagen.peso,
                    raza = animalRequestsinImagen.raza,
                    sexo = animalRequestsinImagen.sexo,
                    edad = animalRequestsinImagen.edad,
                    especie = animalRequestsinImagen.especie,
                    estado = animalRequestsinImagen.estado,
                    fechaSalida = animalRequestsinImagen.fechaSalida,
                    urlImage = uploadResult.url,
                    rescatistaId = animalRequestsinImagen.rescatistaId
                )

                println("DEBUG Enviando a service.createAnimalConRescate")
                val resultado = service.createAnimalConRescate(animalRequest, rescateRequest!!)

                println("DEBUG Resultado createAnimalConRescate: $resultado")

                if (resultado != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(true, "Animal y rescate creados exitosamente", resultado)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(false, "Error al crear animal y rescate")
                    )
                }

            } catch (e: IllegalArgumentException) {
                println("DEBUG IllegalArgumentException: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "Datos inválidos: ${e.message}")
                )

            } catch (e: Exception) {
                println("DEBUG Exception general: ${e.message}")
                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error: ${e.message}")
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

                val multipart = call.receiveMultipart()

                var animalRequestsinImagen: AnimalRequestsinImagen? = null
                var rescateRequest: RescateRequestSinAnimalId? = null
                var imageBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "animal" -> {
                                    animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                }
                                "rescate" -> {
                                    rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "imagen") {
                                imageBytes = part.streamProvider().readBytes()
                                contentType = part.contentType?.toString() ?: "image/jpeg"
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                if (animalRequestsinImagen == null || rescateRequest == null) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de animal y rescate son requeridos"
                        )
                    )
                }

                val animalActual = service.getAnimalById(id)
                var urlImageFinal = animalActual?.urlImage ?: ""

                if (imageBytes != null) {
                    val uploadResult = S3Service.uploadAnimalImage(imageBytes!!, contentType!!)
                    if (uploadResult.success) {
                        urlImageFinal = uploadResult.url
                    }
                }

                val animalRequest = AnimalRequest(
                    nombre = animalRequestsinImagen.nombre,
                    peso = animalRequestsinImagen.peso,
                    raza = animalRequestsinImagen.raza,
                    sexo = animalRequestsinImagen.sexo,
                    edad = animalRequestsinImagen.edad,
                    especie = animalRequestsinImagen.especie,
                    estado = animalRequestsinImagen.estado,
                    fechaSalida = animalRequestsinImagen.fechaSalida,
                    urlImage = urlImageFinal,
                    rescatistaId = animalRequestsinImagen.rescatistaId
                )

                val resultado = service.updateAnimalConRescate(id, animalRequest, rescateRequest)

                if (resultado != null) {
                    val mensaje = if (imageBytes != null) {
                        "Animal, rescate e imagen actualizados exitosamente"
                    } else {
                        "Animal y rescate actualizados exitosamente"
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = mensaje,
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