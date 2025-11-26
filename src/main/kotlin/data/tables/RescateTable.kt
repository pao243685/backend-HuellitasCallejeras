package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Rescates : Table("rescate") {
    val id = uuid("id_rescate").clientDefault { UUID.randomUUID() }
    val fechaIngreso = timestamp("fecha_ingreso")
    val lugar = varchar("lugar", 250)
    val descripcion = varchar("descripcion", 250)
    val animalId = uuid("animal_id").references(animal.id)

    override val primaryKey = PrimaryKey(id)
}