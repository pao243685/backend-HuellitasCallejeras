package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Citas : Table("citas") {
    val id = uuid("id_citas").clientDefault { UUID.randomUUID() }
    val fechaRealizacion = timestamp("fecha_realizacion")
    val fechaCita = timestamp("fecha_cita")
    val titulo = varchar("titulo", 250)
    val motivo = varchar("motivo", 250)
    val lugar = varchar("lugar", 250)
    val animalId = uuid("animal_id").references(animal.id)

    override val primaryKey = PrimaryKey(id)
}