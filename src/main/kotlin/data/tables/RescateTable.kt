package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Rescates : Table("rescate") {
    val id = integer("id_rescate").autoIncrement()
    val fechaIngreso = timestamp("fecha_ingreso")
    val lugar = varchar("lugar", 250)
    val descripcion = varchar("descripcion", 250)
    val animalitoId = integer("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}