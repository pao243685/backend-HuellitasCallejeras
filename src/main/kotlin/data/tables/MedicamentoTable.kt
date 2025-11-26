package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Medicamentos : Table("medicamentos") {
    val id = uuid("id_medicamento").clientDefault { UUID.randomUUID() }
    val nombre = varchar("nombre", 250)

    override val primaryKey = PrimaryKey(id)
}