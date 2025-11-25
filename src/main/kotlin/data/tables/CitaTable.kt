package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Citas : Table("citas") {
    val id = integer("id_citas").autoIncrement()
    val fechaRealizacion = timestamp("fecha_realizacion")
    val fechaCita = timestamp("fecha_cita")
    val motivo = varchar("motivo", 250)
    val lugar = varchar("lugar", 250)
    val animalitoId = integer("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}