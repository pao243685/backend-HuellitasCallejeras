package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.data.tables.Medicamentos
import com.example.domain.models.MedicamentoTratamiento
import com.example.domain.models.Tratamiento
import com.example.domain.models.TratamientoRequest
import com.example.data.tables.TratamientoAnimal
import com.example.data.tables.TratamientoMedicamento
import com.example.data.tables.Tratamientos
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
    suspend fun updateTratamiento(id: UUID, receta: String?): Boolean
    suspend fun deleteTratamiento(id: UUID): Boolean
    suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento>
    suspend fun updateRecetaArchivoUrl(id: UUID, recetaArchivoUrl: String): Boolean
    suspend fun replaceMedicamentos(tratamientoId: UUID, medicamentos: List<MedicamentoTratamiento>): Boolean
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

    override suspend fun updateTratamiento(id: UUID, receta: String?): Boolean = dbQuery {
        val actual = Tratamientos
            .select { Tratamientos.id eq id }
            .singleOrNull()
            ?: return@dbQuery false

        val recetaFinal = receta ?: actual[Tratamientos.receta]

        Tratamientos.update({ Tratamientos.id eq id }) {
            it[Tratamientos.receta] = recetaFinal
        } > 0
    }

    override suspend fun deleteTratamiento(id: UUID): Boolean = dbQuery {
        TratamientoMedicamento.deleteWhere { tratamientoId eq id }
        TratamientoAnimal.deleteWhere { tratamientoId eq id }
        Tratamientos.deleteWhere { Tratamientos.id eq id } > 0
    }

    override suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento> = dbQuery {
        (TratamientoMedicamento innerJoin Medicamentos)
            .select { TratamientoMedicamento.tratamientoId eq tratamientoId }
            .map {
                MedicamentoTratamiento(
                    medicamentoId = it[TratamientoMedicamento.medicamentoId],
                    nombre = it[Medicamentos.nombre],
                    dosis = it[TratamientoMedicamento.dosis],
                    repeticion = it[TratamientoMedicamento.repeticion],
                    fechaConclusion = it[TratamientoMedicamento.fechaConclusion]
                )
            }
    }

    override suspend fun updateRecetaArchivoUrl(id: UUID, recetaArchivoUrl: String): Boolean = dbQuery {
        Tratamientos.update({ Tratamientos.id eq id }) {
            it[Tratamientos.receta] = recetaArchivoUrl
        } > 0
    }

    override suspend fun replaceMedicamentos(
        tratamientoId: UUID,
        medicamentos: List<MedicamentoTratamiento>
    ): Boolean = dbQuery {

        TratamientoMedicamento.deleteWhere { TratamientoMedicamento.tratamientoId eq tratamientoId }

        medicamentos.forEach { med ->
            TratamientoMedicamento.insert {
                it[TratamientoMedicamento.tratamientoId] = tratamientoId
                it[medicamentoId] = med.medicamentoId
                it[dosis] = med.dosis
                it[repeticion] = med.repeticion
                it[fechaConclusion] = med.fechaConclusion
            }
        }

        true
        }
}