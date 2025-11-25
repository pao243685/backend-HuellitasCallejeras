package com.example.data.tables

import org.jetbrains.exposed.sql.Table

object Medicamentos : Table("medicamentos") {
    val id = integer("id_medicamento").autoIncrement()
    val nombre = varchar("nombre", 250)

    override val primaryKey = PrimaryKey(id)
}