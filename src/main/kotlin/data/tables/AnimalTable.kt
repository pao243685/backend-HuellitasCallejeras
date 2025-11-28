package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object animal : Table("animal") {
    val id = uuid("id_animal").clientDefault { UUID.randomUUID() }
    val nombre = varchar("nombre", 250)
    val peso = float("peso")
    val raza = varchar("raza", 250).nullable()
    val sexo = varchar("sexo", 250)
    val edad = integer("edad")
    val especie = varchar("especie", 250)
    val estado = varchar("estado", 250)
    val fechaSalida = timestamp("fecha_salida").nullable()
    val urlImage = varchar("url_imagen", 250)
    val rescatistaId = uuid("rescatista_id").references(Rescatistas.id)

    override val primaryKey = PrimaryKey(id)
}