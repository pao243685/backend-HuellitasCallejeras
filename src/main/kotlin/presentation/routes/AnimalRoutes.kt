package com.example.presentation.routes

import com.example.domain.models.AnimalRequest
import com.example.domain.models.AnimalRequestsinImagen
import com.example.domain.models.ApiResponse
import com.example.domain.models.RescateRequestSinAnimalId
import com.example.domain.models.services.AnimalService
import com.example.domain.models.services.S3Service
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

fun Route.animalRoutes(service: AnimalService) {

    suspend fun deleteImageFromS3(fileName: String): Boolean {
        return try {
            S3Service.deleteImage(fileName)
        } catch (e: Exception) {
            println("Error eliminando imagen de S3: ${e.message}")
            false
        }
    }

    fun extractFileNameFromS3Url(url: String): String {
        return try {
            S3Service.extractFileNameFromS3Url(url)
        } catch (e: Exception) {
            println("Error extrayendo nombre de archivo: ${e.message}")
            throw e
        }
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
                            val s3Deleted = deleteImageFromS3(fileName)

                            if (s3Deleted) {
                                println("Imagen eliminada de S3: $fileName")
                            } else {
                                println("No se pudo eliminar la imagen de S3: $fileName")
                            }
                        } catch (e: Exception) {
                            println("Error eliminando imagen de S3: ${e.message}")
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
                                    try {
                                        animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                    } catch (e: Exception) {
                                        println("Error parseando animal: ${e.message}")
                                    }
                                }
                                "rescate" -> {
                                    try {
                                        rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                    } catch (e: Exception) {
                                        println("Error parseando rescate: ${e.message}")
                                    }
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "imagen") {
                                try {
                                    imageBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "image/jpeg"
                                } catch (e: Exception) {
                                    println("Error leyendo imagen: ${e.message}")
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (animalRequestsinImagen == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de animal son requeridos"
                        )
                    )
                }

                if (rescateRequest == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de rescate son requeridos"
                        )
                    )
                }

                if (imageBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "La imagen es requerida"
                        )
                    )
                }

                val uploadResult = S3Service.uploadAnimalImage(imageBytes!!, contentType!!)
                if (!uploadResult.success) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error subiendo imagen: ${uploadResult.message}"
                        )
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

                val resultado = service.createAnimalConRescate(animalRequest, rescateRequest!!)
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
                                    try {
                                        animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                    } catch (e: Exception) {
                                        println("Error parseando animal: ${e.message}")
                                    }
                                }
                                "rescate" -> {
                                    try {
                                        rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                    } catch (e: Exception) {
                                        println("Error parseando rescate: ${e.message}")
                                    }
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "imagen") {
                                try {
                                    imageBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "image/jpeg"
                                } catch (e: Exception) {
                                    println("Error procesando nueva imagen: ${e.message}")
                                }
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