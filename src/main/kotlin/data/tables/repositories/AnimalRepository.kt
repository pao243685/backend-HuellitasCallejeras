package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Animalito
import com.example.domain.models.AnimalitoRequest
import com.example.domain.models.AnimalitoRescateResponse
import com.example.domain.models.Rescate
import com.example.domain.models.RescateRequestSinAnimalitoId
import com.example.tables.Animalitos
import com.example.tables.Rescates
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
    suspend fun createAnimalitoConRescate(
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse?

    suspend fun updateAnimalitoConRescate(
        animalId: UUID,
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse?

    suspend fun getAnimalitoConRescate(animalId: UUID): AnimalitoRescateResponse?
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

    override suspend fun createAnimalitoConRescate(
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse? = dbQuery {
        val transactionResult = try {
            // 1. Crear el animalito
            val animalitoInsert = Animalitos.insert {
                it[nombre] = animalRequest.nombre
                it[peso] = animalRequest.peso
                it[raza] = animalRequest.raza
                it[sexo] = animalRequest.sexo
                it[edad] = animalRequest.edad
                it[especie] = animalRequest.especie
                it[estado] = animalRequest.estado
                it[urlImage] = animalRequest.urlImage
            }

            val NanimalitoId = animalitoInsert[Animalitos.id]

            val rescateInsert = Rescates.insert {
                it[fechaIngreso] = Instant.now()
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
                it[animalitoId] =  NanimalitoId
            }

            val animalitoCreado = animalitoInsert.resultedValues?.singleOrNull()?.toAnimalito()
                ?: throw Exception("Error al obtener animalito creado")

            val rescateCreado = rescateInsert.resultedValues?.singleOrNull()?.let { row ->
                Rescate(
                    id = row[Rescates.id],
                    fechaIngreso = row[Rescates.fechaIngreso],
                    lugar = row[Rescates.lugar],
                    descripcion = row[Rescates.descripcion],
                    animalitoId = row[Rescates.animalitoId]
                )
            } ?: throw Exception("Error al crear rescate")

            AnimalitoRescateResponse(animalitoCreado, rescateCreado)

        } catch (e: Exception) {
            throw e
        }

        transactionResult
    }
    override suspend fun updateAnimalitoConRescate(
        animalId: UUID,
        animalRequest: AnimalitoRequest,
        rescateRequest: RescateRequestSinAnimalitoId
    ): AnimalitoRescateResponse? = dbQuery {

        val animalitoActualizado = Animalitos.update({ Animalitos.id eq animalId }) {
            it[nombre] = animalRequest.nombre
            it[peso] = animalRequest.peso
            it[raza] = animalRequest.raza
            it[sexo] = animalRequest.sexo
            it[edad] = animalRequest.edad
            it[especie] = animalRequest.especie
            it[estado] = animalRequest.estado
            it[urlImage] = animalRequest.urlImage
            if (animalRequest.estado == "Adoptado") {
                it[fechaSalida] = Instant.now()
            }
        } > 0

        if (!animalitoActualizado) {
            return@dbQuery null
        }


        val rescateExistente = Rescates.select { Rescates.animalitoId eq animalId }.singleOrNull()

        val rescateActualizado = if (rescateExistente != null) {

            Rescates.update({ Rescates.animalitoId eq animalId }) {
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
            } > 0
        } else {

            Rescates.insert {
                it[fechaIngreso] = Instant.now()
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
                it[animalitoId] = animalId
            }.insertedCount > 0
        }

        if (!rescateActualizado) {
            return@dbQuery null
        }

        val animalito = getAnimalitoById(animalId)
        val rescate = Rescates.select { Rescates.animalitoId eq animalId }
            .map { it.toRescate() }
            .singleOrNull()

        if (animalito != null && rescate != null) {
            AnimalitoRescateResponse(animalito, rescate)
        } else {
            null
        }
    }

    override suspend fun getAnimalitoConRescate(animalId: UUID): AnimalitoRescateResponse? = dbQuery {
        val animalito = getAnimalitoById(animalId)
        val rescate = Rescates.select { Rescates.animalitoId eq animalId }
            .map { it.toRescate() }
            .singleOrNull()

        if (animalito != null && rescate != null) {
            AnimalitoRescateResponse(animalito, rescate)
        } else {
            null
        }
    }

    private fun ResultRow.toRescate() = Rescate(
        id = this[Rescates.id],
        fechaIngreso = this[Rescates.fechaIngreso],
        lugar = this[Rescates.lugar],
        descripcion = this[Rescates.descripcion],
        animalitoId = this[Rescates.animalitoId]
    )

}

