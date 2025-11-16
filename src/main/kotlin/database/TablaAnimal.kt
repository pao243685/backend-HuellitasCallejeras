package com.example.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import java.util.UUID


object TablaAnimal : Table("animal") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }    // val id = uuid("id").autoGenerate()
    val nombre = varchar("nombre", 255)
    val especie = varchar("especie", 255)
    val raza = varchar("raza", 255)
    val sexo = varchar("sexo", 10)
    val peso = double("peso")
    val edad = integer("edad")
    val fechaSalida = date("fecha_salida").nullable()
    val estado = varchar("estado", 255)
    override val primaryKey = PrimaryKey(id)

}