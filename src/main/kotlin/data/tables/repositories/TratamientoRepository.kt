package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
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
    suspend fun updateTratamiento(id: UUID, receta: String): Boolean
    suspend fun deleteTratamiento(id: UUID): Boolean
    suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento>
}

class TratamientoRepositoryImpl : TratamientoRepository {

    private fun ResultRow.toTratamiento() = Tratamiento(
        id = this[Tratamientos.id],
        fechaInicio = this[Tratamientos.fechaInicio],
        receta = this[Tratamientos.receta]
    )

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
        val tratamientoId = Tratamientos.insert {
            it[fechaInicio] = Instant.now()
            it[receta] = request.receta
        }[Tratamientos.id]

        // Asociar con animal
        TratamientoAnimal.insert {
            it[animalId] = request.animalId
            it[TratamientoAnimal.tratamientoId] = tratamientoId
        }

        // Asociar medicamentos
        request.medicamentos.forEach { med ->
            TratamientoMedicamento.insert {
                it[TratamientoMedicamento.tratamientoId] = tratamientoId
                it[medicamentoId] = med.medicamentoId
                it[dosis] = med.dosis
                it[repeticion] = med.repeticion
                it[fechaConclusion] = med.fechaConclusion
            }
        }

        getTratamientoById(tratamientoId)
    }

    override suspend fun updateTratamiento(id: UUID, receta: String): Boolean = dbQuery {
        Tratamientos.update({ Tratamientos.id eq id }) {
            it[Tratamientos.receta] = receta
        } > 0
    }

    override suspend fun deleteTratamiento(id: UUID): Boolean = dbQuery {
        TratamientoMedicamento.deleteWhere { tratamientoId eq id }
        TratamientoAnimal.deleteWhere { tratamientoId eq id }
        Tratamientos.deleteWhere { Tratamientos.id eq id } > 0
    }

    override suspend fun getMedicamentosByTratamiento(tratamientoId: UUID): List<MedicamentoTratamiento> = dbQuery {
        TratamientoMedicamento
            .select { TratamientoMedicamento.tratamientoId eq tratamientoId }
            .map {
                MedicamentoTratamiento(
                    medicamentoId = it[TratamientoMedicamento.medicamentoId],
                    dosis = it[TratamientoMedicamento.dosis],
                    repeticion = it[TratamientoMedicamento.repeticion],
                    fechaConclusion = it[TratamientoMedicamento.fechaConclusion]
                )
            }
    }
}