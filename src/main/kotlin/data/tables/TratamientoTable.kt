package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Tratamientos : Table("tratamiento") {
    val id = uuid("id_tratamiento").clientDefault { UUID.randomUUID() }
    val fechaInicio = timestamp("fecha_inicio")
    val receta = varchar("receta", 250)

    override val primaryKey = PrimaryKey(id)
}