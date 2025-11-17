package com.example.config

import com.example.data.tables.repositories.AnimalRepositoryImpl
import com.example.data.tables.repositories.CitaRepositoryImpl
import com.example.data.tables.repositories.MedicamentoRepositoryImpl
import com.example.data.tables.repositories.RescateRepositoryImpl
import com.example.data.tables.repositories.TratamientoRepositoryImpl
import com.example.domain.models.services.AnimalService
import com.example.domain.models.services.CitaService
import com.example.domain.models.services.MedicamentoService
import com.example.domain.models.services.RescateService
import com.example.domain.models.services.TratamientoService
import com.example.presentation.routes.animalRoutes
import com.example.presentation.routes.citaRoutes
import com.example.presentation.routes.medicamentoRoutes
import com.example.presentation.routes.rescateRoutes
import com.example.presentation.routes.tratamientoRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val animalitoRepository = AnimalRepositoryImpl()
    val rescateRepository = RescateRepositoryImpl()
    val tratamientoRepository = TratamientoRepositoryImpl()
    val medicamentoRepository = MedicamentoRepositoryImpl()
    val citaRepository = CitaRepositoryImpl()

    // Inicializar servicios
    val animalitoService = AnimalService(animalitoRepository)
    val rescateService = RescateService(rescateRepository)
    val tratamientoService = TratamientoService(tratamientoRepository)
    val medicamentoService = MedicamentoService(medicamentoRepository)
    val citaService = CitaService(citaRepository)

    routing {
        route("/api") {
            // Rutas de la API
            animalRoutes(animalitoService)
            rescateRoutes(rescateService)
            tratamientoRoutes(tratamientoService)
            medicamentoRoutes(medicamentoService)
            citaRoutes(citaService)

            // Ruta de prueba
            get("/health") {
                call.respond(mapOf("status" to "OK", "message" to "API funcionando correctamente"))
            }
        }
    }
}