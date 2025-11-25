package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Animalitos : Table("animalito") {
    val id = integer("id_animalito").autoIncrement()
    val nombre = varchar("nombre", 250)
    val peso = float("peso")
    val raza = varchar("raza", 250).nullable()
    val sexo = varchar("sexo", 250)
    val edad = integer("edad")
    val especie = varchar("especie", 250)
    val estado = varchar("estado", 250)
    val fechaSalida = timestamp("fecha_salida").nullable()

    override val primaryKey = PrimaryKey(id)
}