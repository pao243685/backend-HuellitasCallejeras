package com.example.config

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
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting() {
    val rescatistaRepository = RescatistaRepositoryImpl()
    val animalRepository = AnimalRepositoryImpl()
    val rescateRepository = RescateRepositoryImpl()
    val tratamientoRepository = TratamientoRepositoryImpl()
    val medicamentoRepository = MedicamentoRepositoryImpl()
    val citaRepository = CitaRepositoryImpl()

    val authService = AuthService(rescatistaRepository)
    val animalService = AnimalService(animalRepository)
    val tratamientoService = TratamientoService(tratamientoRepository)
    val medicamentoService = MedicamentoService(medicamentoRepository)
    val citaService = CitaService(citaRepository)

    routing {

        staticFiles("/uploads", File("uploads")) {
            default("index.html")
            enableAutoHeadResponse()
        }
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

            authenticate("auth-jwt") {
                animalRoutes(animalService)
                tratamientoRoutes(tratamientoService)
                medicamentoRoutes(medicamentoService)
                citaRoutes(citaService)
            }
        }
    }
}