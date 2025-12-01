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
            println("=== INICIANDO CREAR-CON-RESCATE ===")

            try {
                val multipart = call.receiveMultipart()
                println("✅ Multipart recibido correctamente")

                var animalRequestsinImagen: AnimalRequestsinImagen? = null
                var rescateRequest: RescateRequestSinAnimalId? = null
                var imageBytes: ByteArray? = null
                var contentType: String? = null

                var partsProcessed = 0
                multipart.forEachPart { part ->
                    partsProcessed++
                    println("🔍 Procesando parte $partsProcessed - Tipo: ${part::class.simpleName}, Nombre: ${part.name}")

                    when (part) {
                        is PartData.FormItem -> {
                            println("📝 FormItem: ${part.name} = ${part.value.take(100)}...")
                            when (part.name) {
                                "animal" -> {
                                    try {
                                        println("🐾 Parseando animal JSON...")
                                        animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                        println("✅ Animal parseado: $animalRequestsinImagen")
                                    } catch (e: Exception) {
                                        println("❌ Error parseando animal: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                                "rescate" -> {
                                    try {
                                        println("🚑 Parseando rescate JSON...")
                                        rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                        println("✅ Rescate parseado: $rescateRequest")
                                    } catch (e: Exception) {
                                        println("❌ Error parseando rescate: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                                else -> {
                                    println("⚠️  FormItem desconocido: ${part.name}")
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            println("📁 FileItem: ${part.name}, Nombre archivo: ${part.originalFileName}, ContentType: ${part.contentType}")
                            if (part.name == "imagen") {
                                try {
                                    println("🖼️  Procesando imagen...")
                                    imageBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "image/jpeg"
                                    println("✅ Imagen leída: ${imageBytes?.size ?: 0} bytes, ContentType: $contentType")
                                } catch (e: Exception) {
                                    println("❌ Error leyendo imagen: ${e.message}")
                                    e.printStackTrace()
                                }
                            } else {
                                println("⚠️  FileItem no manejado: ${part.name}")
                            }
                        }
                        else -> {
                            println("⚠️  Tipo de parte no manejado: ${part::class.simpleName}")
                        }
                    }
                    part.dispose()
                }

                println("=== VALIDACIÓN DE DATOS ===")
                println("📊 Partes procesadas: $partsProcessed")
                println("🐾 Animal: ${animalRequestsinImagen?.let { "PRESENTE" } ?: "FALTANTE"}")
                println("🚑 Rescate: ${rescateRequest?.let { "PRESENTE" } ?: "FALTANTE"}")
                println("🖼️  Imagen: ${imageBytes?.let { "${it.size} bytes" } ?: "FALTANTE"}")

                if (animalRequestsinImagen == null) {
                    println("❌ ERROR: Datos de animal son requeridos")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de animal son requeridos"
                        )
                    )
                }

                if (rescateRequest == null) {
                    println("❌ ERROR: Datos de rescate son requeridos")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de rescate son requeridos"
                        )
                    )
                }

                if (imageBytes == null) {
                    println("❌ ERROR: La imagen es requerida")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "La imagen es requerida"
                        )
                    )
                }

                println("=== SUBIENDO IMAGEN A S3 ===")
                val uploadResult = S3Service.uploadAnimalImage(imageBytes!!, contentType!!)
                println("📤 Resultado S3: éxito=${uploadResult.success}, mensaje=${uploadResult.message}, url=${uploadResult.url}")

                if (!uploadResult.success) {
                    println("❌ ERROR subiendo imagen a S3")
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error subiendo imagen: ${uploadResult.message}"
                        )
                    )
                }

                println("=== CREANDO ANIMAL CON RESCATE ===")
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

                println("🐾 AnimalRequest creado: $animalRequest")
                println("🚑 RescateRequest: $rescateRequest")

                val resultado = service.createAnimalConRescate(animalRequest, rescateRequest!!)

                if (resultado != null) {
                    println("✅ ÉXITO: Animal y rescate creados")
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            message = "Animal y rescate creados exitosamente",
                            data = resultado
                        )
                    )
                } else {
                    println("❌ ERROR: Service devolvió null")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error al crear animal y rescate"
                        )
                    )
                }

            } catch (e: IllegalArgumentException) {
                println("❌ IllegalArgumentException: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(
                        success = false,
                        message = "Datos inválidos: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                println("❌ Exception general: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            } finally {
                println("=== FINALIZANDO CREAR-CON-RESCATE ===\n")
            }
        }

        put("/{id}/actualizar-con-rescate") {
            try {
                println("🔍 [DEBUG] Iniciando actualización con rescate...")

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                println("🔍 [DEBUG] ID recibido: ${call.parameters["id"]}, UUID convertido: $id")

                if (id == null) {
                    println("❌ [DEBUG] ID inválido o nulo")
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
                println("🔍 [DEBUG] Multipart recibido correctamente")

                var animalRequestsinImagen: AnimalRequestsinImagen? = null
                var rescateRequest: RescateRequestSinAnimalId? = null
                var imageBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            println("🔍 [DEBUG] Procesando FormItem - name: ${part.name}, value: ${part.value.take(200)}...")
                            when (part.name) {
                                "animal" -> {
                                    try {
                                        println("🔍 [DEBUG] Intentando parsear JSON animal...")
                                        animalRequestsinImagen = Json.decodeFromString<AnimalRequestsinImagen>(part.value)
                                        println("✅ [DEBUG] Animal parseado exitosamente: $animalRequestsinImagen")
                                    } catch (e: Exception) {
                                        println("❌ [DEBUG] Error parseando animal: ${e.message}")
                                        println("❌ [DEBUG] JSON animal problemático: ${part.value}")
                                        e.printStackTrace()
                                    }
                                }
                                "rescate" -> {
                                    try {
                                        println("🔍 [DEBUG] Intentando parsear JSON rescate...")
                                        rescateRequest = Json.decodeFromString<RescateRequestSinAnimalId>(part.value)
                                        println("✅ [DEBUG] Rescate parseado exitosamente: $rescateRequest")
                                    } catch (e: Exception) {
                                        println("❌ [DEBUG] Error parseando rescate: ${e.message}")
                                        println("❌ [DEBUG] JSON rescate problemático: ${part.value}")
                                        e.printStackTrace()
                                    }
                                }
                                else -> {
                                    println("🔍 [DEBUG] FormItem desconocido: ${part.name} = ${part.value}")
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            println("🔍 [DEBUG] Procesando FileItem - name: ${part.name}, filename: ${part.originalFileName}")
                            if (part.name == "imagen") {
                                try {
                                    imageBytes = part.streamProvider().readBytes()
                                    contentType = part.contentType?.toString() ?: "image/jpeg"
                                    println("✅ [DEBUG] Imagen procesada - tamaño: ${imageBytes?.size ?: 0} bytes, tipo: $contentType")
                                } catch (e: Exception) {
                                    println("❌ [DEBUG] Error procesando nueva imagen: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        }
                        else -> {
                            println("🔍 [DEBUG] PartData de tipo desconocido: ${part::class.simpleName}")
                        }
                    }
                    part.dispose()
                }

                println("🔍 [DEBUG] Resumen después de procesar multipart:")
                println("   - Animal: ${animalRequestsinImagen != null}")
                println("   - Rescate: ${rescateRequest != null}")
                println("   - Imagen: ${imageBytes != null} (${imageBytes?.size ?: 0} bytes)")

                if (animalRequestsinImagen == null || rescateRequest == null) {
                    println("❌ [DEBUG] Faltan datos requeridos - animal: ${animalRequestsinImagen == null}, rescate: ${rescateRequest == null}")
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Any>(
                            success = false,
                            message = "Datos de animal y rescate son requeridos"
                        )
                    )
                }

                // Verificar animal existente
                val animalActual = service.getAnimalById(id)
                println("🔍 [DEBUG] Animal actual en BD: $animalActual")
                var urlImageFinal = animalActual?.urlImage ?: ""
                println("🔍 [DEBUG] URL imagen actual: $urlImageFinal")

                if (imageBytes != null) {
                    println("🔍 [DEBUG] Subiendo nueva imagen a S3...")
                    val uploadResult = S3Service.uploadAnimalImage(imageBytes!!, contentType!!)
                    println("🔍 [DEBUG] Resultado upload S3: success=${uploadResult.success}, url=${uploadResult.url}")

                    if (uploadResult.success) {
                        urlImageFinal = uploadResult.url
                        println("✅ [DEBUG] Nueva URL imagen: $urlImageFinal")
                    } else {
                        println("❌ [DEBUG] Falló upload a S3, manteniendo imagen anterior")
                    }
                } else {
                    println("🔍 [DEBUG] No se proporcionó nueva imagen, manteniendo la actual")
                }

                // Construir request final
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

                println("🔍 [DEBUG] AnimalRequest final: $animalRequest")
                println("🔍 [DEBUG] RescateRequest final: $rescateRequest")

                // Llamar al servicio
                println("🔍 [DEBUG] Llamando a service.updateAnimalConRescate...")
                val resultado = service.updateAnimalConRescate(id, animalRequest, rescateRequest)
                println("🔍 [DEBUG] Resultado del servicio: $resultado")

                if (resultado != null) {
                    val mensaje = if (imageBytes != null) {
                        "Animal, rescate e imagen actualizados exitosamente"
                    } else {
                        "Animal y rescate actualizados exitosamente"
                    }

                    println("✅ [DEBUG] Actualización exitosa: $mensaje")
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = mensaje,
                            data = resultado
                        )
                    )
                } else {
                    println("❌ [DEBUG] Animal no encontrado en el servicio")
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Any>(
                            success = false,
                            message = "Animal no encontrado"
                        )
                    )
                }

            } catch (e: IllegalArgumentException) {
                println("❌ [DEBUG] IllegalArgumentException: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(
                        success = false,
                        message = "Datos inválidos: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                println("❌ [DEBUG] Exception general: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            } finally {
                println("🔍 [DEBUG] Finalizando endpoint de actualización")
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