package com.example.config

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import com.example.data.tables.repositories.AnimalRepositoryImpl
import com.example.data.tables.repositories.CitaRepositoryImpl
import com.example.data.tables.repositories.MedicamentoRepositoryImpl
import com.example.data.tables.repositories.RescateRepositoryImpl
import com.example.data.tables.repositories.RescatistaRepositoryImpl
import com.example.data.tables.repositories.TratamientoRepositoryImpl
import com.example.domain.models.services.AnimalService
import com.example.domain.models.services.AuthService
import com.example.domain.models.services.CitaService
import com.example.domain.models.services.MedicamentoService
import com.example.domain.models.services.TratamientoService
import com.example.presentation.routes.animalRoutes
import com.example.presentation.routes.authRoutes
import com.example.presentation.routes.citaRoutes
import com.example.presentation.routes.medicamentoRoutes
import com.example.presentation.routes.tratamientoRoutes
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val rescatistaRepository = RescatistaRepositoryImpl()
    val animalRepository = AnimalRepositoryImpl()
    val rescateRepository = RescateRepositoryImpl()
    val tratamientoRepository = TratamientoRepositoryImpl()
    val medicamentoRepository = MedicamentoRepositoryImpl()
    val citaRepository = CitaRepositoryImpl()

    // Inicializar servicios
    val authService = AuthService(rescatistaRepository)
    val animalService = AnimalService(animalRepository)
    val tratamientoService = TratamientoService(tratamientoRepository)
    val medicamentoService = MedicamentoService(medicamentoRepository)
    val citaService = CitaService(citaRepository)

    routing {
        route("/api") {
            authRoutes(authService)

            get("/health") {
                call.respond(
                    mapOf(
                        "status" to "OK",
                        "message" to "API Huellitas Callejeras funcionando",
                        "version" to "2.0.0"
                    )
                )
            }

            get("/test-s3-connection") {
                try {
                    val env = dotenv {
                        directory = "./"
                        ignoreIfMissing = false
                    }
                    println("Bucket: ${env["AWS_S3_BUCKET"]}")

                    val s3Client = S3Client {
                        region = "us-east-1"
                        credentialsProvider = StaticCredentialsProvider(
                            Credentials(
                                accessKeyId = env["AWS_ACCESS_KEY_ID"] ?: "",
                                secretAccessKey = env["AWS_SECRET_ACCESS_KEY"] ?: "",
                                sessionToken = env["AWS_SESSION_TOKEN"]
                            )
                        )
                    }

                    println("Cliente S3 creado:  ${s3Client}")

                    call.respond("Cliente S3 - Bucket: ${env["AWS_S3_BUCKET"]}")

                } catch (e: Exception) {
                    println("Error creando cliente S3: ${e.message}")
                    e.printStackTrace()

                    call.respond("Error: ${e.message}")
                }
            }

            authenticate("auth-jwt") {
                animalRoutes(animalService)
                tratamientoRoutes(tratamientoService)
                medicamentoRoutes(medicamentoService)
                citaRoutes(citaService)
            }
        }
    }
}