package com.example.domain.models.services

import com.example.domain.models.FileUploadResponse
import java.io.File
import java.util.*

object FileService {

    private val baseUploadDir = File("uploads")
    private val animalsDir = File(baseUploadDir, "animals")
    private val treatmentsDir = File(baseUploadDir, "tratamientos")

    init {
        animalsDir.mkdirs()
        treatmentsDir.mkdirs()
        println("FileService inicializado con directorio: ${baseUploadDir.absolutePath}")
    }

    suspend fun uploadAnimalImage(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg"
    ): FileUploadResponse {
        return try {
            val fileExtension = getExtension(contentType)
            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val file = File(animalsDir, fileName)

            file.writeBytes(imageBytes)

            val imageUrl = "/uploads/animals/$fileName"

            FileUploadResponse(
                success = true,
                message = "Imagen de animal subida exitosamente",
                url = imageUrl
            )

        } catch (e: Exception) {
            println("Error en uploadAnimalImage: ${e.message}")
            e.printStackTrace()
            FileUploadResponse(
                success = false,
                message = "Error subiendo imagen: ${e.message}",
                url = ""
            )
        }
    }

    suspend fun uploadFile(
        fileBytes: ByteArray,
        contentType: String,
        folder: String = "tratamientos"
    ): FileUploadResponse {
        return try {
            val fileExtension = getExtension(contentType)
            val fileName = "${UUID.randomUUID()}.$fileExtension"

            val targetDir = when (folder) {
                "tratamientos" -> treatmentsDir
                "animals" -> animalsDir
                else -> File(baseUploadDir, folder).also { it.mkdirs() }
            }

            val file = File(targetDir, fileName)
            file.writeBytes(fileBytes)

            val fileUrl = "/uploads/$folder/$fileName"

            FileUploadResponse(
                success = true,
                message = "Archivo subido exitosamente",
                url = fileUrl
            )

        } catch (e: Exception) {
            println("Error subiendo archivo: ${e.message}")
            FileUploadResponse(
                success = false,
                message = "Error subiendo archivo: ${e.message}",
                url = ""
            )
        }
    }

    suspend fun deleteFile(filePath: String): Boolean {
        return try {
            // Extraer el path relativo (ej: /uploads/animals/filename.jpg -> animals/filename.jpg)
            val relativePath = filePath.removePrefix("/uploads/")
            val file = File(baseUploadDir, relativePath)

            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    println("Archivo eliminado: ${file.absolutePath}")
                }
                deleted
            } else {
                println("Archivo no encontrado: ${file.absolutePath}")
                false
            }
        } catch (e: Exception) {
            println("Error eliminando archivo $filePath: ${e.message}")
            false
        }
    }


    private fun getExtension(contentType: String): String {
        return when (contentType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            else -> "bin"
        }
    }

}