package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Medicamento
import com.example.domain.models.MedicamentoRequest
import com.example.tables.Medicamentos
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface MedicamentoRepository {
    suspend fun getAllMedicamentos(): List<Medicamento>
    suspend fun getMedicamentoById(id: UUID): Medicamento?
    suspend fun createMedicamento(request: MedicamentoRequest): Medicamento?
    suspend fun updateMedicamento(id: UUID, nombre: String): Boolean
    suspend fun deleteMedicamento(id: UUID): Boolean
}

class MedicamentoRepositoryImpl : MedicamentoRepository {

    private fun ResultRow.toMedicamento() = Medicamento(
        id = this[Medicamentos.id],
        nombre = this[Medicamentos.nombre]
    )

    override suspend fun getAllMedicamentos(): List<Medicamento> = dbQuery {
        Medicamentos.selectAll().map { it.toMedicamento() }
    }

    override suspend fun getMedicamentoById(id: UUID): Medicamento? = dbQuery {
        Medicamentos.select { Medicamentos.id eq id }
            .map { it.toMedicamento() }
            .singleOrNull()
    }

    override suspend fun createMedicamento(request: MedicamentoRequest): Medicamento? = dbQuery {
        val insertStatement = Medicamentos.insert {
            it[nombre] = request.nombre
        }

        insertStatement.resultedValues?.singleOrNull()?.toMedicamento()
    }

    override suspend fun updateMedicamento(id: UUID, nombre: String): Boolean = dbQuery {
        Medicamentos.update({ Medicamentos.id eq id }) {
            it[Medicamentos.nombre] = nombre
        } > 0
    }

    override suspend fun deleteMedicamento(id: UUID): Boolean = dbQuery {
        Medicamentos.deleteWhere { Medicamentos.id eq id } > 0
    }
}