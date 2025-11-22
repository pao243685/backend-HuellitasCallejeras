package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Rescate
import com.example.domain.models.RescateRequest
import com.example.tables.Rescates
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

interface RescateRepository {
    suspend fun getAllRescates(): List<Rescate>
    suspend fun getRescateById(id: UUID): Rescate?
    suspend fun getRescatesByAnimalito(animalitoId: UUID): List<Rescate>
    suspend fun createRescate(request: RescateRequest): Rescate?
    suspend fun updateRescate(id: UUID, request: RescateRequest): Boolean
    suspend fun deleteRescate(id: UUID): Boolean
}

class RescateRepositoryImpl : RescateRepository {

    private fun ResultRow.toRescate() = Rescate(
        id = this[Rescates.id],
        fechaIngreso = this[Rescates.fechaIngreso],
        lugar = this[Rescates.lugar],
        descripcion = this[Rescates.descripcion],
        animalitoId = this[Rescates.animalitoId]
    )

    override suspend fun getAllRescates(): List<Rescate> = dbQuery {
        Rescates.selectAll().map { it.toRescate() }
    }

    override suspend fun getRescateById(id: UUID): Rescate? = dbQuery {
        Rescates.select { Rescates.id eq id }
            .map { it.toRescate() }
            .singleOrNull()
    }

    override suspend fun getRescatesByAnimalito(animalitoId: UUID): List<Rescate> = dbQuery {
        Rescates.select { Rescates.animalitoId eq animalitoId }
            .map { it.toRescate() }
    }

    override suspend fun createRescate(request: RescateRequest): Rescate? = dbQuery {
        val insertStatement = Rescates.insert {
            it[fechaIngreso] = Instant.now()
            it[lugar] = request.lugar
            it[descripcion] = request.descripcion
            it[animalitoId] = request.animalitoId
        }

        insertStatement.resultedValues?.singleOrNull()?.toRescate()
    }

    override suspend fun updateRescate(id: UUID, request: RescateRequest): Boolean = dbQuery {
        Rescates.update({ Rescates.id eq id }) {
            it[lugar] = request.lugar
            it[descripcion] = request.descripcion
            it[animalitoId] = request.animalitoId
        } > 0
    }

    override suspend fun deleteRescate(id: UUID): Boolean = dbQuery {
        Rescates.deleteWhere { Rescates.id eq id } > 0
    }
}