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

        println("S3 DEBUG: Iniciando uploadAnimalImage")
        println("S3 DEBUG: Bucket -> $bucketName")
        println("S3 DEBUG: Region -> $region")
        println("S3 DEBUG: Tamaño bytes -> ${imageBytes.size}")
        println("S3 DEBUG: Content-Type -> $contentType")

        return try {
            val fileExtension = getExtension(contentType)
            val fileName = "animals/${UUID.randomUUID()}.$fileExtension"

            println("S3 DEBUG: Archivo a subir -> $fileName")

            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(imageBytes)
                this.contentType = contentType
            }

            println("S3 DEBUG: Enviando solicitud a S3...")
            s3Client.putObject(request)
            println("S3 DEBUG: Upload completado con éxito")

            val imageUrl = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
            println("S3 DEBUG: URL generada -> $imageUrl")

            com.example.domain.models.FileUploadResponse(
                success = true,
                message = "Imagen de animal subida exitosamente",
                url = imageUrl
            )

        } catch (e: Exception) {
            println("S3 ERROR en uploadAnimalImage: ${e.message}")
            e.printStackTrace()

            com.example.domain.models.FileUploadResponse(false, "Error subiendo imagen", "")
        }
    }

    suspend fun uploadImageFromUrl(imageUrl: String): com.example.domain.models.FileUploadResponse {
        println("S3 DEBUG: Iniciando upload desde URL -> $imageUrl")

        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()

            val bytes = connection.getInputStream().readAllBytes()
            val contentType = connection.contentType ?: "image/jpeg"

            println("S3 DEBUG: Bytes descargados -> ${bytes.size}")
            println("S3 DEBUG: Tipo detectado -> $contentType")

            uploadAnimalImage(bytes, contentType)

        } catch (e: Exception) {
            println("S3 ERROR en uploadImageFromUrl: ${e.message}")
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
        println("S3 DEBUG: Eliminando archivo -> $fileName")

        return try {
            val req = DeleteObjectRequest {
                bucket = bucketName
                key = fileName
            }

            s3Client.deleteObject(req)
            println("S3 DEBUG: Eliminación exitosa")
            true

        } catch (e: Exception) {
            println("S3 ERROR al eliminar: ${e.message}")
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

        println("S3 DEBUG: Iniciando uploadFile")
        println("S3 DEBUG: folder -> $folder")
        println("S3 DEBUG: bytes -> ${fileBytes.size}")

        return try {
            val extension = getExtension(contentType)
            val fileName = "$folder/${UUID.randomUUID()}.$extension"

            println("S3 DEBUG: Archivo a subir -> $fileName")

            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }

            println("S3 DEBUG: Subiendo archivo...")
            s3Client.putObject(request)
            println("S3 DEBUG: Upload completado")

            val url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
            println("S3 DEBUG: URL resultante -> $url")

            com.example.domain.models.FileUploadResponse(true, "Archivo subido", url)

        } catch (e: Exception) {
            println("S3 ERROR en uploadFile: ${e.message}")
            com.example.domain.models.FileUploadResponse(false, "Error subiendo archivo", "")
        }
    }
}

