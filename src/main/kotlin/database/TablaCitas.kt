package com.example.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import kotlinx.datetime.LocalDate
import java.util.UUID

object TablaCitas: Table("citas") {
    val id = uuid("id_citas").clientDefault { UUID.randomUUID() }
    val fecha_realizacion = date("fecha_realizacion")
    val fecha_cita = date("fecha_cita")
    val motivo = varchar("motivo", 255)
    val lugar = varchar("lugar", 255)
    val animal_id = uuid("animalito_id")
}