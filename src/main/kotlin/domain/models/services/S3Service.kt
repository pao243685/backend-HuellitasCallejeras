package com.example.domain.models.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.ObjectCannedAcl
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import java.util.UUID

object S3Service {

    private val bucketName = System.getenv("AWS_S3_BUCKET") ?: ""
    private val region = System.getenv("AWS_REGION") ?: "us-east-1"
    const val DEFAULT_ANIMAL_IMAGE = "https://amzn-s3-prueba-archivos-bucket.s3.us-east-1.amazonaws.com/imagen-default.jpg"

    private val accessKey = System.getenv("AWS_ACCESS_KEY_ID") ?: ""
    private val secretKey = System.getenv("AWS_SECRET_ACCESS_KEY") ?: ""
    private val sessionToken = System.getenv("AWS_SESSION_TOKEN") ?: ""


    private val credentialsProvider = StaticCredentialsProvider(
        Credentials(
            accessKeyId = accessKey,
            secretAccessKey = secretKey,
            sessionToken = sessionToken
        )
    )

    // 🔥 Cliente S3 listo para usar
    private fun s3Client() = S3Client {
        region = this@S3Service.region
        credentialsProvider = this@S3Service.credentialsProvider
    }

    suspend fun uploadAnimalImage(bytes: ByteArray, contentType: String): String {
        val fileName = "animals/${UUID.randomUUID()}.${getExtension(contentType)}"

        s3Client().use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(bytes)
                this.contentType = contentType
                acl = ObjectCannedAcl.PublicRead
            }

            s3.putObject(request)
        }
        return "https://${bucketName}.s3.${region}.amazonaws.com/$fileName"
    }

    suspend fun uploadRecetaMedica(bytes: ByteArray, contentType: String): String {
        val fileName = "recetas/${UUID.randomUUID()}.${getExtension(contentType)}"

        s3Client().use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(bytes)
                this.contentType = contentType
                acl = ObjectCannedAcl.PublicRead
            }

            s3.putObject(request)
        }

        return "https://${bucketName}.s3.${region}.amazonaws.com/$fileName"
    }

    suspend fun deleteFile(fileUrl: String) {
        val fileName = extractFileNameFromUrl(fileUrl)

        s3Client().use { s3 ->
            val request = DeleteObjectRequest {
                bucket = bucketName
                key = fileName
            }
            s3.deleteObject(request)
        }
    }

    private fun extractFileNameFromUrl(url: String): String {
        return url.substringAfter(".amazonaws.com/")
    }

    private fun getExtension(contentType: String): String {
        return when (contentType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            else -> "bin"
        }
    }
}
