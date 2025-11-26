package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.RescatistaLogin
import com.example.domain.models.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {

        post("/login") {
            try {
                val request = call.receive<RescatistaLogin>()
                val authResponse = authService.login(request)

                if (authResponse != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Login exitoso",
                            data = authResponse
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Any>(
                            success = false,
                            message = "Credenciales inválidas"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Any>(
                        success = false,
                        message = "Error en el login: ${e.message}"
                    )
                )
            }
        }

        post("/crear-rescatista") {
            try {
                val request = call.receive<RescatistaLogin>()
                val rescatista = authService.createRescatista(request.nombre, request.contraseña)

                if (rescatista != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            message = "Rescatista creado exitosamente",
                            data = rescatista
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Any>(
                            success = false,
                            message = "Error al crear rescatista"
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(
                        success = false,
                        message = e.message ?: "Error de validación"
                    )
                )
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