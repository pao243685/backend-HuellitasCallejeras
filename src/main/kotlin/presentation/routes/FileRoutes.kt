package com.example.presentation.routes


import com.example.data.tables.repositories.AnimalRepository
import com.example.data.tables.repositories.TratamientoRepository
import com.example.domain.models.ApiResponse
import com.example.domain.models.FileUploadResponse
import com.example.domain.models.services.S3Service
import com.example.domain.models.services.S3Service.DEFAULT_ANIMAL_IMAGE
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.util.UUID

fun Route.fileRoutes(
    animalRepository: AnimalRepository,
    tratamientoRepository: TratamientoRepository
) {

    authenticate("auth-jwt") {

        // Subir imagen de animal
        post("/animal/{id}/imagen") {
            try {
                val animalId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )

                // Verificar que el animal existe
                val animal = animalRepository.getAnimalById(animalId) ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Any>(false, "Animal no encontrado")
                )

                // Recibir multipart
                val multipart = call.receiveMultipart()
                var imageBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            contentType = part.contentType?.toString()
                            imageBytes = part.provider().readRemaining().readByteArray()
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                if (imageBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "No se envió ninguna imagen")
                    )
                }

                // Validar tipo de archivo
                if (!contentType?.startsWith("image/")!!) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo debe ser una imagen")
                    )
                }

                // Validar tamaño (máximo 5MB)
                if (imageBytes.size > 5 * 1024 * 1024) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "La imagen no debe superar los 5MB")
                    )
                }

                try {
                    S3Service.deleteFile(animal.urlImage)
                } catch (e: Exception) {
                    println("No se pudo eliminar imagen anterior: ${e.message}")
                }

                val imageUrl = S3Service.uploadAnimalImage(imageBytes, contentType!!)

                animalRepository.updateImagenUrl(animalId, imageUrl)

                call.respond(
                    HttpStatusCode.OK,
                    FileUploadResponse(
                        success = true,
                        message = "Imagen subida exitosamente",
                        url = imageUrl
                    )
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error al subir imagen: ${e.message}")
                )
            }
        }

        // Eliminar imagen de animal
        delete("/animal/{id}/imagen") {
            try {
                val animalId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )

                val animal = animalRepository.getAnimalById(animalId)
                if (animal == null) {
                    return@delete call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(false, "Animal no encontrado")
                    )
                }

                S3Service.deleteFile(animal.urlImage)

                // Actualizar en BD
                animalRepository.updateImagenUrl(animalId, DEFAULT_ANIMAL_IMAGE)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, "Imagen eliminada", null)
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error al eliminar imagen: ${e.message}")
                )
            }
        }

        // Subir archivo de receta médica
        post("/tratamientos/{id}/receta") {
            try {
                val tratamientoId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )

                val tratamiento = tratamientoRepository.getTratamientoById(tratamientoId) ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Any>(false, "Tratamiento no encontrado")
                )

                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            contentType = part.contentType?.toString()
                            fileBytes = part.provider().readRemaining().readByteArray()
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                if (fileBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "No se envió ningún archivo")
                    )
                }

                // Validar tipo de archivo (PDF o imágenes)
                val allowedTypes = listOf(
                    "application/pdf",
                    "image/jpeg",
                    "image/jpg",
                    "image/png"
                )
                if (contentType !in allowedTypes) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Solo se permiten archivos PDF o imágenes")
                    )
                }

                // Validar tamaño (máximo 10MB)
                if (fileBytes.size > 10 * 1024 * 1024) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo no debe superar los 10MB")
                    )
                }

                try {
                    S3Service.deleteFile(tratamiento.receta)
                } catch (e: Exception) {
                    println("No se pudo eliminar archivo anterior: ${e.message}")
                }

                // Subir a S3
                val fileUrl = S3Service.uploadRecetaMedica(fileBytes, contentType!!)

                // Actualizar en BD
                tratamientoRepository.updateRecetaArchivoUrl(tratamientoId, fileUrl)

                call.respond(
                    HttpStatusCode.OK,
                    FileUploadResponse(
                        success = true,
                        message = "Receta médica subida exitosamente",
                        url = fileUrl
                    )
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error al subir receta: ${e.message}")
                )
            }
        }

        // Eliminar archivo de receta
        put("/tratamientos/{id}/receta") {
            try {
                val tratamientoId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "ID inválido")
                    )

                val tratamiento = tratamientoRepository.getTratamientoById(tratamientoId)
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(false, "Tratamiento no encontrado")
                    )

                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        contentType = part.contentType?.toString()
                        fileBytes = part.provider().readRemaining().readByteArray()
                    }
                    part.dispose()
                }

                val allowedTypes = listOf(
                    "application/pdf", "image/jpeg", "image/jpg", "image/png"
                )

                if (contentType !in allowedTypes) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "Solo se permiten PDF o imágenes")
                    )
                }

                // ✔ Validar tamaño
                if (fileBytes!!.size > 10 * 1024 * 1024) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(false, "El archivo no debe superar los 10MB")
                    )
                }

                // ✔ Si ya existía una receta → eliminarla de S3
                S3Service.deleteFile(tratamiento.receta)

                val fileUrl = S3Service.uploadRecetaMedica(fileBytes, contentType!!)
                tratamientoRepository.updateRecetaArchivoUrl(tratamientoId, fileUrl)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, "Receta actualizada correctamente", fileUrl)
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(false, "Error al actualizar receta: ${e.message}")
                )
            }
        }
    }
}