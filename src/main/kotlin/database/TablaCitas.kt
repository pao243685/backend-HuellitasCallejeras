package com.example.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

object TablaCitas: Table("citas") {
    val id = uuid("id_citas").clientDefault { UUID.randomUUID() }
    val fecha_realizacion = datetime("fecha_realizacion")
    val fecha_cita = datetime("fecha_cita")
    val motivo = varchar("motivo", 255)
    val lugar = varchar("lugar", 255)
    val animal_id = uuid("animalito_id")
}