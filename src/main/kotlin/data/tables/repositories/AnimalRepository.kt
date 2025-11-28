package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.domain.models.Animal
import com.example.domain.models.AnimalRequest
import com.example.domain.models.AnimalRescateResponse
import com.example.domain.models.Rescate
import com.example.domain.models.RescateRequestSinAnimalId
import com.example.data.tables.Rescates
import com.example.data.tables.animal
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
    suspend fun getAllAnimal(): List<Animal>
    suspend fun getAnimalById(id: UUID): Animal?
    suspend fun createAnimal(request: AnimalRequest): Animal?
    suspend fun updateAnimal(id: UUID, request: AnimalRequest): Boolean
    suspend fun deleteAnimal(id: UUID): Boolean
    suspend fun updateImagenUrl(id: UUID, imagenUrl: String): Boolean
    suspend fun getAnimalByEstado(estado: String): List<Animal>
    suspend fun createAnimalConRescate(
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse?

    suspend fun updateAnimalConRescate(
        animalId: UUID,
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse?

    suspend fun getAnimalConRescate(animalId: UUID): AnimalRescateResponse?
}

class AnimalRepositoryImpl : AnimalRepository {

    private fun ResultRow.toAnimal() = Animal(
        id = this[animal.id],
        nombre = this[animal.nombre],
        peso = this[animal.peso],
        raza = this[animal.raza],
        sexo = this[animal.sexo],
        edad = this[animal.edad],
        especie = this[animal.especie],
        estado = this[animal.estado],
        fechaSalida = this[animal.fechaSalida],
        urlImage = this[animal.urlImage],
        rescatistaId = this[animal.rescatistaId],
    )

    override suspend fun getAllAnimal(): List<Animal> = dbQuery {
        animal.selectAll().map { it.toAnimal() }
    }

    override suspend fun getAnimalById(id: UUID): Animal? = dbQuery {
        animal.select { animal.id eq id }
            .map { it.toAnimal() }
            .singleOrNull()
    }

    override suspend fun createAnimal(request: AnimalRequest): Animal? = dbQuery {
        val insertStatement = animal.insert {
            it[nombre] = request.nombre
            it[peso] = request.peso
            it[raza] = request.raza
            it[sexo] = request.sexo
            it[edad] = request.edad
            it[especie] = request.especie
            it[estado] = request.estado
            it[urlImage] = request.urlImage
            it[rescatistaId] = request.rescatistaId
        }

        insertStatement.resultedValues?.singleOrNull()?.toAnimal()
    }

    override suspend fun updateAnimal(id: UUID, request: AnimalRequest): Boolean = dbQuery {
        animal.update({ animal.id eq id }) {
            it[nombre] = request.nombre
            it[peso] = request.peso
            it[raza] = request.raza
            it[sexo] = request.sexo
            it[edad] = request.edad
            it[especie] = request.especie
            it[estado] = request.estado
            it[urlImage] = request.urlImage
            it[rescatistaId] = request.rescatistaId
            if (request.estado == "Adoptado") {
                it[fechaSalida] = Instant.now()
            }
        } > 0
    }

    override suspend fun deleteAnimal(id: UUID): Boolean = dbQuery {
        animal.deleteWhere { animal.id eq id } > 0
    }

    override suspend fun getAnimalByEstado(estado: String): List<Animal> = dbQuery {
        animal.select { animal.estado eq estado }
            .map { it.toAnimal() }
    }

    override suspend fun createAnimalConRescate(
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse? = dbQuery {
        val transactionResult = try {
            val animalInsert = animal.insert {
                it[nombre] = animalRequest.nombre
                it[peso] = animalRequest.peso
                it[raza] = animalRequest.raza
                it[sexo] = animalRequest.sexo
                it[edad] = animalRequest.edad
                it[especie] = animalRequest.especie
                it[estado] = animalRequest.estado
                it[urlImage] = animalRequest.urlImage
                it[rescatistaId] = animalRequest.rescatistaId
            }

            val animalId = animalInsert[animal.id]

            val rescateInsert = Rescates.insert {
                it[fechaIngreso] = Instant.now()
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
                it[this.animalId] =  animalId
            }

            val animalCreado = animalInsert.resultedValues?.singleOrNull()?.toAnimal()
                ?: throw Exception("Error al obtener animal creado")

            val rescateCreado = rescateInsert.resultedValues?.singleOrNull()?.let { row ->
                Rescate(
                    id = row[Rescates.id],
                    fechaIngreso = row[Rescates.fechaIngreso],
                    lugar = row[Rescates.lugar],
                    descripcion = row[Rescates.descripcion],
                    animalId = row[Rescates.animalId]
                )
            } ?: throw Exception("Error al crear rescate")

            AnimalRescateResponse(animalCreado, rescateCreado)

        } catch (e: Exception) {
            throw e
        }

        transactionResult
    }
    override suspend fun updateAnimalConRescate(
        animalId: UUID,
        animalRequest: AnimalRequest,
        rescateRequest: RescateRequestSinAnimalId
    ): AnimalRescateResponse? = dbQuery {

        val animalActualizado = animal.update({ animal.id eq animalId }) {
            it[nombre] = animalRequest.nombre
            it[peso] = animalRequest.peso
            it[raza] = animalRequest.raza
            it[sexo] = animalRequest.sexo
            it[edad] = animalRequest.edad
            it[especie] = animalRequest.especie
            it[estado] = animalRequest.estado
            it[urlImage] = animalRequest.urlImage
            it[rescatistaId] = animalRequest.rescatistaId
            if (animalRequest.estado == "Adoptado") {
                it[fechaSalida] = Instant.now()
            }
        } > 0

        if (!animalActualizado) {
            return@dbQuery null
        }


        val rescateExistente = Rescates.select { Rescates.animalId eq animalId }.singleOrNull()

        val rescateActualizado = if (rescateExistente != null) {

            Rescates.update({ Rescates.animalId eq animalId }) {
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
            } > 0
        } else {

            Rescates.insert {
                it[fechaIngreso] = Instant.now()
                it[lugar] = rescateRequest.lugar
                it[descripcion] = rescateRequest.descripcion
            }.insertedCount > 0
        }

        if (!rescateActualizado) {
            return@dbQuery null
        }

        val animal = getAnimalById(animalId)
        val rescate = Rescates.select { Rescates.animalId eq animalId }
            .map { it.toRescate() }
            .singleOrNull()

        if (animal != null && rescate != null) {
            AnimalRescateResponse(animal, rescate)
        } else {
            null
        }
    }

    override suspend fun getAnimalConRescate(animalId: UUID): AnimalRescateResponse? = dbQuery {
        val animal = getAnimalById(animalId)
        val rescate = Rescates.select { Rescates.animalId eq animalId }
            .map { it.toRescate() }
            .singleOrNull()

        if (animal != null && rescate != null) {
            AnimalRescateResponse(animal, rescate)
        } else {
            null
        }
    }


    override suspend fun updateImagenUrl(id: UUID, imagenUrl: String): Boolean = dbQuery {
        animal.update({ animal.id eq id }) {
            it[animal.urlImage] = imagenUrl
        } > 0
    }
    private fun ResultRow.toRescate() = Rescate(
        id = this[Rescates.id],
        fechaIngreso = this[Rescates.fechaIngreso],
        lugar = this[Rescates.lugar],
        descripcion = this[Rescates.descripcion],
        animalId = this[Rescates.animalId]
    )

}

