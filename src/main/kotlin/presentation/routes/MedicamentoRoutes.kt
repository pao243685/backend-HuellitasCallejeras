package com.example.presentation.routes

import com.example.domain.models.ApiResponse
import com.example.domain.models.MedicamentoRequest
import com.example.domain.models.services.MedicamentoService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.util.UUID

fun Route.medicamentoRoutes(service: MedicamentoService) {
    route("/medicamentos") {

        get {
            val medicamentos = service.getAllMedicamentos()
            call.respond(ApiResponse(true, "Medicamentos obtenidos", medicamentos))
        }

        get("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val medicamento = service.getMedicamentoById(id)
            if (medicamento != null) {
                call.respond(ApiResponse(true, "Medicamento encontrado", medicamento))
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Any>(false, "No encontrado")
                )
            }
        }

        post {
            try {
                val request = call.receive<MedicamentoRequest>()
                val medicamento = service.createMedicamento(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(true, "Medicamento creado", medicamento)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, e.message ?: "Error")
                )
            }
        }

        put("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val nombre = call.receive<Map<String, String>>()["nombre"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "Nombre requerido")
                )

            val updated = service.updateMedicamento(id, nombre)
            call.respond(
                if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(
                    updated,
                    if (updated) "Actualizado" else "No encontrado",
                    null
                )
            )
        }

        delete("/{id}") {
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(false, "ID inválido (debe ser UUID)")
                )
            }

            val deleted = service.deleteMedicamento(id)
            call.respond(
                if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound,
                ApiResponse(
                    deleted,
                    if (deleted) "Eliminado" else "No encontrado",
                    null
                )
            )
        }
    }
}