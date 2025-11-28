package com.example.data.tables.repositories

import com.example.config.DatabaseFactory.dbQuery
import com.example.data.tables.Rescatistas
import com.example.domain.models.Rescatista
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

interface RescatistaRepository {
    suspend fun getRescatistaById(id: UUID): Rescatista?
    suspend fun getRescatistaByNombre(nombre: String): Pair<Rescatista, String>?
    suspend fun createRescatista(nombre: String, contraseña: String): Rescatista?
}

class RescatistaRepositoryImpl : RescatistaRepository {

    private fun ResultRow.toRescatista() = Rescatista(
        id = this[Rescatistas.id],
        nombre = this[Rescatistas.nombre]
    )

    override suspend fun getRescatistaById(id: UUID): Rescatista? = dbQuery {
        Rescatistas.select { Rescatistas.id eq id }
            .map { it.toRescatista() }
            .singleOrNull()
    }

    override suspend fun getRescatistaByNombre(nombre: String): Pair<Rescatista, String>? = dbQuery {
        Rescatistas.select { Rescatistas.nombre eq nombre }
            .map { it.toRescatista() to it[Rescatistas.contrasena] }
            .singleOrNull()
    }

    override suspend fun createRescatista(nombre: String, contraseña: String): Rescatista? = dbQuery {
        val id = UUID.randomUUID()
        Rescatistas.insert {
            it[Rescatistas.id] = id
            it[Rescatistas.nombre] = nombre
            it[Rescatistas.contrasena] = contraseña
        }

        Rescatista(id, nombre)
    }
}