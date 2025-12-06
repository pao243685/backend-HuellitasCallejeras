package com.example.domain.models.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.ByteStream
import io.github.cdimascio.dotenv.dotenv
import java.net.URL
import java.util.*

object S3Service {

    private val env = dotenv {
        directory = "./"
        ignoreIfMissing = false
    }

    private val bucketName = "aws-s3-huellitas-callejeras-2"
    private val region = "us-east-1"
    private val accessKey = "ASIAX2MJDTXZGY52MY5I"
    private val secretKey = "SIPg9em1MvhNsIRJ3JOEJOa11/yy7E2IadBr/JJe"
    private val sessionToken = "IQoJb3JpZ2luX2VjEKP//////////wEaCXVzLXdlc3QtMiJIMEYCIQD5OuNODkku+yLF8H6vj/jrqaWhcSym1WdTu9J0ASuL0gIhALiNHhp/UgfwWN1rFOoM5ajawoxv1GPVgpK/k3FcDuheKrcCCGwQAhoMNTM3Njk1MzI5Nzc4IgwDPXu3s/CijkxXZIAqlAIBQXS7euud29CwjtVB8wx6nwO4ngr4Op761/dzU6TJA8KPACkYsEADypEHWidjC4tVFpyLWYGtKmeIOkntS1WXmqUSzkcky5pm9TULSyJYoLp9pfbCOwPdJpzttPH7mZnxsEcDGFsm+xeAnirujHsNKORLEet+P1iS5lROISZCUas3jNhp7cE97uqREfuGoc/61fGzpBFVvZ+dtuTRjDfSOWOVOeJA7i/IdftX5Ih6no+ZN7+GM6fCxlFeyWoHVkihzZIOIqUwzI9tiykXuIVhbLM+d1po+kIEo+zf9e74/JGl79EzxvfHzKVtYom5s7ZHqCWtGQNb7bglQYSPOlsjMhlcpq+BTGcIb41kvIUnZgyEGrww4bDOyQY6nAFByhRtrfcfDmxRZmLHEgY1m6qM1tPOc238W4xHD+FLO9oLyDws/ufUOHdIPzIwgsZer9POUvaXz6FKDfO0+mJgXAg8znW7AEcOCNhM1vSQ+/psQs7GaigP6imaWJvMuQzeIV1NOAIFpFVXj27eXrhD1S90oEnri0NdqmbBTT69BRbScoFYk/jP90bw4dPuFPY8bqFGbeukT50x1nM="

    init {
        println("S3 Service con Bucket: $bucketName")
    }

    private val s3Client by lazy {
        S3Client {
            region = this@S3Service.region
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = accessKey,
                    secretAccessKey = secretKey,
                    sessionToken = if (sessionToken.isNotBlank()) sessionToken else null
                )
            )
        }
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
            e.printStackTrace()
            com.example.domain.models.FileUploadResponse(
                success = false,
                message = "Error subiendo imagen: ${e.message}",
                url = ""
            )
        }
    }

    suspend fun uploadImageFromUrl(imageUrl: String): com.example.domain.models.FileUploadResponse {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val imageBytes = inputStream.readAllBytes()
            inputStream.close()

            val contentType = connection.contentType ?: "image/jpeg"

            uploadAnimalImage(imageBytes, contentType)

        } catch (e: Exception) {
            com.example.domain.models.FileUploadResponse(
                success = false,
                message = "Error procesando imagen desde URL: ${e.message}",
                url = ""
            )
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

    suspend fun deleteImage(fileName: String): Boolean {
        return try {

            val request = DeleteObjectRequest {
                bucket = bucketName
                key = fileName
            }

            s3Client.deleteObject(request)
            println("Archivo eliminado de s3: $fileName")
            true
        } catch (e: Exception) {
            println("Error eliminando archivo de s3 $fileName: ${e.message}")
            false
        }
    }

    fun extractFileNameFromS3Url(url: String): String {
        return try {
            val prefix = "https://$bucketName.s3.$region.amazonaws.com/"
            if (url.startsWith(prefix)) {
                url.substring(prefix.length)
            } else {
                url.substringAfter("amazonaws.com/")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("URL de S3 inválida: $url")
        }
    }


    suspend fun uploadFile(
        fileBytes: ByteArray,
        contentType: String,
        folder: String = "files"
    ): com.example.domain.models.FileUploadResponse {
        return try {
            val fileExtension = getExtension(contentType)
            val fileName = "$folder/${UUID.randomUUID()}.$fileExtension"

            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }

            val response = s3Client.putObject(request)

            val fileUrl = "https://$bucketName.s3.$region.amazonaws.com/$fileName"

            com.example.domain.models.FileUploadResponse(
                success = true,
                message = "Archivo subido exitosamente",
                url = fileUrl
            )

        } catch (e: Exception) {
            println("Error subiendo archivo: ${e.message}")
            com.example.domain.models.FileUploadResponse(
                success = false,
                message = "Error subiendo archivo: ${e.message}",
                url = ""
            )
        }
    }

}