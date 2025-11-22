package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Animalito
import com.example.domain.models.AnimalitoRequest
import com.example.tables.Animalitos
import java.time.Instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface AnimalRepository {
    suspend fun getAllAnimalitos(): List<Animalito>
    suspend fun getAnimalitoById(id: UUID): Animalito?
    suspend fun createAnimalito(request: AnimalitoRequest): Animalito?
    suspend fun updateAnimalito(id: UUID, request: AnimalitoRequest): Boolean
    suspend fun deleteAnimalito(id: UUID): Boolean
    suspend fun getAnimalitosByEstado(estado: String): List<Animalito>
}

class AnimalRepositoryImpl : AnimalRepository {

    private fun ResultRow.toAnimalito() = Animalito(
        id = this[Animalitos.id],
        nombre = this[Animalitos.nombre],
        peso = this[Animalitos.peso],
        raza = this[Animalitos.raza],
        sexo = this[Animalitos.sexo],
        edad = this[Animalitos.edad],
        especie = this[Animalitos.especie],
        estado = this[Animalitos.estado],
        fechaSalida = this[Animalitos.fechaSalida],
        urlImage = this[Animalitos.urlImage],
    )

    override suspend fun getAllAnimalitos(): List<Animalito> = dbQuery {
        Animalitos.selectAll().map { it.toAnimalito() }
    }

    override suspend fun getAnimalitoById(id: UUID): Animalito? = dbQuery {
        Animalitos.select { Animalitos.id eq id }
            .map { it.toAnimalito() }
            .singleOrNull()
    }

    override suspend fun createAnimalito(request: AnimalitoRequest): Animalito? = dbQuery {
        val insertStatement = Animalitos.insert {
            it[nombre] = request.nombre
            it[peso] = request.peso
            it[raza] = request.raza
            it[sexo] = request.sexo
            it[edad] = request.edad
            it[especie] = request.especie
            it[estado] = request.estado
            it[urlImage] = request.urlImage
        }

        insertStatement.resultedValues?.singleOrNull()?.toAnimalito()
    }

    override suspend fun updateAnimalito(id: UUID, request: AnimalitoRequest): Boolean = dbQuery {
        Animalitos.update({ Animalitos.id eq id }) {
            it[nombre] = request.nombre
            it[peso] = request.peso
            it[raza] = request.raza
            it[sexo] = request.sexo
            it[edad] = request.edad
            it[especie] = request.especie
            it[estado] = request.estado
            it[urlImage] = request.urlImage
            if (request.estado == "Adoptado") {
                it[fechaSalida] = Instant.now()
            }
        } > 0
    }

    override suspend fun deleteAnimalito(id: UUID): Boolean = dbQuery {
        Animalitos.deleteWhere { Animalitos.id eq id } > 0
    }

    override suspend fun getAnimalitosByEstado(estado: String): List<Animalito> = dbQuery {
        Animalitos.select { Animalitos.estado eq estado }
            .map { it.toAnimalito() }
    }
}