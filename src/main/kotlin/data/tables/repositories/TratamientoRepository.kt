package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.data.tables.Medicamentos
import com.example.domain.models.MedicamentoTratamiento
import com.example.domain.models.Tratamiento
import com.example.domain.models.TratamientoRequest
import com.example.data.tables.TratamientoAnimal
import com.example.data.tables.TratamientoMedicamento
import com.example.data.tables.Tratamientos
import com.example.data.tables.animal
import com.example.domain.models.Animal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

interface TratamientoRepository {
    suspend fun getAllTratamientos(): List<Tratamiento>
    suspend fun getTratamientoById(id: UUID): Tratamiento?
    suspend fun getTratamientosByAnimal(animalId: UUID): List<Tratamiento>
    suspend fun createTratamiento(request: TratamientoRequest): Tratamiento?
    suspend fun updateTratamiento(id: UUID, request: TratamientoRequest): Boolean
    suspend fun deleteTratamiento(id: UUID): Boolean
    suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento>
    suspend fun updateRecetaArchivoUrl(id: UUID, recetaArchivoUrl: String): Boolean
}

class TratamientoRepositoryImpl : TratamientoRepository {

    private fun ResultRow.toTratamiento(): Tratamiento {
        return Tratamiento(
            id = this[Tratamientos.id],
            fechaInicio = this[Tratamientos.fechaInicio],
            receta = this[Tratamientos.receta]
        )
    }

    override suspend fun getAllTratamientos(): List<Tratamiento> = dbQuery {
        Tratamientos.selectAll().map { it.toTratamiento() }
    }

    override suspend fun getTratamientoById(id: UUID): Tratamiento? = dbQuery {
        Tratamientos.select { Tratamientos.id eq id }
            .map { it.toTratamiento() }
            .singleOrNull()
    }

    override suspend fun getTratamientosByAnimal(animalId: UUID): List<Tratamiento> = dbQuery {
        (Tratamientos innerJoin TratamientoAnimal)
            .select { TratamientoAnimal.animalId eq animalId }
            .map {
                Tratamiento(
                    id = it[Tratamientos.id],
                    fechaInicio = it[Tratamientos.fechaInicio],
                    receta = it[Tratamientos.receta]
                )
            }
    }

    override suspend fun createTratamiento(request: TratamientoRequest): Tratamiento? = dbQuery {
        try {
            val tratamientoId = Tratamientos.insert {
                it[fechaInicio] = request.fechaInicio
                it[receta] = request.receta
            }[Tratamientos.id]

            TratamientoAnimal.insert {
                it[animalId] = request.animalId
                it[TratamientoAnimal.tratamientoId] = tratamientoId
            }

            request.medicamentos.forEach { med ->
                // Obtener el nombre del medicamento
                val nombreMedicamento = Medicamentos
                    .select { Medicamentos.id eq med.medicamentoId }
                    .map { it[Medicamentos.nombre] }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("Medicamento no encontrado: ${med.medicamentoId}")

                TratamientoMedicamento.insert {
                    it[TratamientoMedicamento.tratamientoId] = tratamientoId
                    it[medicamentoId] = med.medicamentoId
                    it[dosis] = med.dosis
                    it[repeticion] = med.repeticion
                    it[fechaConclusion] = med.fechaConclusion
                }
            }

            return@dbQuery Tratamiento(
                id = tratamientoId,
                fechaInicio = request.fechaInicio,
                receta = request.receta
            )

        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateTratamiento(id: UUID, request: TratamientoRequest): Boolean = dbQuery {
        try {
            println("🔵 [DEBUG-REPOSITORY] Actualizando tratamiento ID: $id")
            println("🔵 [DEBUG-REPOSITORY] Request: $request")

            // Verificar si el tratamiento existe
            val tratamientoExistente = Tratamientos.select { Tratamientos.id eq id }.firstOrNull()
            if (tratamientoExistente == null) {
                println("🔴 [ERROR-REPOSITORY] Tratamiento no encontrado: $id")
                return@dbQuery false
            }

            // Verificar si el animal existe
            val animalExiste = animal.select { animal.id eq request.animalId }.firstOrNull()
            if (animalExiste == null) {
                println("🔴 [ERROR-REPOSITORY] Animal no encontrado: ${request.animalId}")
                throw IllegalArgumentException("El animal con ID ${request.animalId} no existe")
            }

            // Actualizar el tratamiento principal
            val tratamientoUpdated = Tratamientos.update({ Tratamientos.id eq id }) {
                it[fechaInicio] = request.fechaInicio
                it[receta] = request.receta
            } > 0

            // Verificar si existe la relación tratamiento-animal
            val relacionExistente = TratamientoAnimal.select {
                TratamientoAnimal.tratamientoId eq id
            }.firstOrNull()

            if (relacionExistente != null) {
                // Actualizar la relación existente
                TratamientoAnimal.update({ TratamientoAnimal.tratamientoId eq id }) {
                    it[animalId] = request.animalId
                }
            } else {
                // Crear nueva relación
                TratamientoAnimal.insert {
                    it[tratamientoId] = id
                    it[animalId] = request.animalId
                }
            }

            // Eliminar medicamentos existentes y agregar los nuevos
            println("🔵 [DEBUG-REPOSITORY] Eliminando medicamentos existentes...")
            TratamientoMedicamento.deleteWhere { TratamientoMedicamento.tratamientoId eq id }

            println("🔵 [DEBUG-REPOSITORY] Insertando ${request.medicamentos.size} nuevos medicamentos...")
            request.medicamentos.forEachIndexed { index, med ->
                println("🔵 [DEBUG-REPOSITORY] Insertando medicamento $index: $med")

                // Verificar que el medicamento existe y obtener su nombre
                val medicamentoExiste = Medicamentos.select { Medicamentos.id eq med.medicamentoId }.firstOrNull()
                if (medicamentoExiste == null) {
                    println("🔴 [ERROR-REPOSITORY] Medicamento no encontrado: ${med.medicamentoId}")
                    throw IllegalArgumentException("El medicamento con ID ${med.medicamentoId} no existe")
                }

                TratamientoMedicamento.insert {
                    it[TratamientoMedicamento.tratamientoId] = id
                    it[medicamentoId] = med.medicamentoId
                    it[dosis] = med.dosis
                    it[repeticion] = med.repeticion
                    it[fechaConclusion] = med.fechaConclusion
                }
            }

            println("🟢 [SUCCESS-REPOSITORY] Tratamiento actualizado exitosamente")
            return@dbQuery true

        } catch (e: Exception) {
            println("🔴 [ERROR-REPOSITORY] Error en updateTratamiento: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deleteTratamiento(id: UUID): Boolean = dbQuery {
        TratamientoMedicamento.deleteWhere { tratamientoId eq id }
        TratamientoAnimal.deleteWhere { tratamientoId eq id }
        Tratamientos.deleteWhere { Tratamientos.id eq id } > 0
    }

    override suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento> = dbQuery {
        try {
            println("🔵 [DEBUG-REPOSITORY] Obteniendo medicamentos para tratamiento: $tratamientoId")

            val resultados = (TratamientoMedicamento innerJoin Medicamentos)
                .select { TratamientoMedicamento.tratamientoId eq tratamientoId }
                .toList()

            println("🔵 [DEBUG-REPOSITORY] Encontrados ${resultados.size} medicamentos")

            return@dbQuery resultados.map {
                val medicamento = MedicamentoTratamiento(
                    medicamentoId = it[TratamientoMedicamento.medicamentoId],
                    nombre = it[Medicamentos.nombre],
                    dosis = it[TratamientoMedicamento.dosis],
                    repeticion = it[TratamientoMedicamento.repeticion],
                    fechaConclusion = it[TratamientoMedicamento.fechaConclusion]
                )
                println("🔵 [DEBUG-REPOSITORY] Medicamento: $medicamento")
                medicamento
            }
        } catch (e: Exception) {
            println("🔴 [ERROR-REPOSITORY] Error obteniendo medicamentos: ${e.message}")
            throw e
        }
    }

    override suspend fun updateRecetaArchivoUrl(id: UUID, recetaArchivoUrl: String): Boolean = dbQuery {
        Tratamientos.update({ Tratamientos.id eq id }) {
            it[Tratamientos.receta] = recetaArchivoUrl
        } > 0
    }
}