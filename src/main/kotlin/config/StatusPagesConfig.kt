package com.example.config

import com.example.domain.models.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Error no manejado", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Any>(
                    success = false,
                    message = "Error interno del servidor: ${cause.message}"
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Any>(
                    success = false,
                    message = "Solicitud inválida: ${cause.message}"
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Any>(
                    success = false,
                    message = "Recurso no encontrado"
                )
            )
        }
    }
}