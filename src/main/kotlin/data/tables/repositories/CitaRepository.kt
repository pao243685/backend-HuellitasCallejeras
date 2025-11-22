package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Cita
import com.example.domain.models.CitaRequest
import com.example.tables.Citas
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

interface CitaRepository {
    suspend fun getAllCitas(): List<Cita>
    suspend fun getCitaById(id: UUID): Cita?
    suspend fun getCitasByAnimalito(animalitoId: UUID): List<Cita>
    suspend fun getCitasPendientes(): List<Cita>
    suspend fun createCita(request: CitaRequest): Cita?
    suspend fun updateCita(id: UUID, request: CitaRequest): Boolean
    suspend fun deleteCita(id: UUID): Boolean
}

class CitaRepositoryImpl : CitaRepository {

    private fun ResultRow.toCita() = Cita(
        id = this[Citas.id],
        fechaRealizacion = this[Citas.fechaRealizacion],
        fechaCita = this[Citas.fechaCita],
        motivo = this[Citas.motivo],
        lugar = this[Citas.lugar],
        animalitoId = this[Citas.animalitoId]
    )

    override suspend fun getAllCitas(): List<Cita> = dbQuery {
        Citas.selectAll().map { it.toCita() }
    }

    override suspend fun getCitaById(id: UUID): Cita? = dbQuery {
        Citas.select { Citas.id eq id }
            .map { it.toCita() }
            .singleOrNull()
    }

    override suspend fun getCitasByAnimalito(animalitoId: UUID): List<Cita> = dbQuery {
        Citas.select { Citas.animalitoId eq animalitoId }
            .orderBy(Citas.fechaCita to SortOrder.DESC)
            .map { it.toCita() }
    }

    override suspend fun getCitasPendientes(): List<Cita> = dbQuery {
        Citas.select { Citas.fechaCita greater Instant.now() }
            .orderBy(Citas.fechaCita to SortOrder.ASC)
            .map { it.toCita() }
    }

    override suspend fun createCita(request: CitaRequest): Cita? = dbQuery {
        val insertStatement = Citas.insert {
            it[fechaRealizacion] = Instant.now()
            it[fechaCita] = request.fechaCita
            it[motivo] = request.motivo
            it[lugar] = request.lugar
            it[animalitoId] = request.animalitoId
        }

        insertStatement.resultedValues?.singleOrNull()?.toCita()
    }

    override suspend fun updateCita(id: UUID, request: CitaRequest): Boolean = dbQuery {
        Citas.update({ Citas.id eq id }) {
            it[fechaCita] = request.fechaCita
            it[motivo] = request.motivo
            it[lugar] = request.lugar
            it[animalitoId] = request.animalitoId
        } > 0
    }

    override suspend fun deleteCita(id: UUID): Boolean = dbQuery {
        Citas.deleteWhere { Citas.id eq id } > 0
    }
}