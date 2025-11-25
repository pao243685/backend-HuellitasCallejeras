package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Tratamientos : Table("tratamiento") {
    val id = integer("id_tratamiento").autoIncrement()
    val fechaInicio = timestamp("fecha_inicio")
    val receta = varchar("receta", 250)

    override val primaryKey = PrimaryKey(id)
}
