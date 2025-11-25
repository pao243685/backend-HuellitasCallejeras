package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.MedicamentoTratamiento
import com.example.domain.models.Tratamiento
import com.example.domain.models.TratamientoRequest
import com.example.data.tables.TratamientoAnimalito
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


interface TratamientoRepository {
    suspend fun getAllTratamientos(): List<Tratamiento>
    suspend fun getTratamientoById(id: Int): Tratamiento?
    suspend fun getTratamientosByAnimalito(animalitoId: Int): List<Tratamiento>
    suspend fun createTratamiento(request: TratamientoRequest): Tratamiento?
    suspend fun updateTratamiento(id: Int, receta: String): Boolean
    suspend fun deleteTratamiento(id: Int): Boolean
    suspend fun getMedicamentosByTratamiento(tratamientoId: Int): List<MedicamentoTratamiento>
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

    override suspend fun getTratamientoById(id: Int): Tratamiento? = dbQuery {
        Tratamientos.select { Tratamientos.id eq id }
            .map { it.toTratamiento() }
            .singleOrNull()
    }

    override suspend fun getTratamientosByAnimalito(animalitoId: Int): List<Tratamiento> = dbQuery {
        (Tratamientos innerJoin TratamientoAnimalito)
            .select { TratamientoAnimalito.animalitoId eq animalitoId }
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

        // Asociar con animalito
        TratamientoAnimalito.insert {
            it[animalitoId] = request.animalitoId
            it[TratamientoAnimalito.tratamientoId] = tratamientoId
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

    override suspend fun updateTratamiento(id: Int, receta: String): Boolean = dbQuery {
        Tratamientos.update({ Tratamientos.id eq id }) {
            it[Tratamientos.receta] = receta
        } > 0
    }

    override suspend fun deleteTratamiento(id: Int): Boolean = dbQuery {
        TratamientoMedicamento.deleteWhere { tratamientoId eq id }
        TratamientoAnimalito.deleteWhere { tratamientoId eq id }
        Tratamientos.deleteWhere { Tratamientos.id eq id } > 0
    }

    override suspend fun getMedicamentosByTratamiento(tratamientoId: Int): List<MedicamentoTratamiento> = dbQuery {
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