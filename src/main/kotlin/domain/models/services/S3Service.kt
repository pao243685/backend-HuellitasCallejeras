package com.example.domain.models.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import java.net.URL
import java.util.*

object S3Service {

    private const val bucketName = "aws-s3-huellitas-callejeras-2"
    private const val region = "us-east-1"


    private val s3Client = S3Client {
        this.region = S3Service.region
    }

    suspend fun uploadAnimalImage(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg"
    ): com.example.domain.models.FileUploadResponse {
        return try {
            val fileExtension = getExtension(contentType)
            val fileName = "animals/${UUID.randomUUID()}.$fileExtension"

            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(imageBytes)
                this.contentType = contentType
            }

            s3Client.putObject(request)

            val imageUrl = "https://$bucketName.s3.$region.amazonaws.com/$fileName"

            com.example.domain.models.FileUploadResponse(
                success = true,
                message = "Imagen de animal subida exitosamente",
                url = imageUrl
            )

        } catch (e: Exception) {
            println("Error en uploadAnimalImage: ${e.message}")
            com.example.domain.models.FileUploadResponse(false, "Error subiendo imagen", "")
        }
    }

    suspend fun uploadImageFromUrl(imageUrl: String): com.example.domain.models.FileUploadResponse {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()

            val bytes = connection.getInputStream().readAllBytes()
            val contentType = connection.contentType ?: "image/jpeg"

            uploadAnimalImage(bytes, contentType)

        } catch (e: Exception) {
            com.example.domain.models.FileUploadResponse(false, "Error procesando URL", "")
        }
    }

    private fun getExtension(contentType: String): String =
        when (contentType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            else -> "bin"
        }

    suspend fun deleteImage(fileName: String): Boolean {
        return try {
            val req = DeleteObjectRequest {
                bucket = bucketName
                key = fileName
            }

            s3Client.deleteObject(req)
            true

        } catch (e: Exception) {
            println("Error al eliminar: ${e.message}")
            false
        }
    }

    fun extractFileNameFromS3Url(url: String): String {
        val prefix = "https://$bucketName.s3.$region.amazonaws.com/"
        return if (url.startsWith(prefix)) {
            url.substring(prefix.length)
        } else {
            url.substringAfter("amazonaws.com/")
        }
    }

    suspend fun uploadFile(
        fileBytes: ByteArray,
        contentType: String,
        folder: String = "files"
    ): com.example.domain.models.FileUploadResponse {
        return try {
            val extension = getExtension(contentType)
            val fileName = "$folder/${UUID.randomUUID()}.$extension"

            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }

            s3Client.putObject(request)

            val url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"

            com.example.domain.models.FileUploadResponse(true, "Archivo subido", url)

        } catch (e: Exception) {
            com.example.domain.models.FileUploadResponse(false, "Error subiendo archivo", "")
        }
    }
}
