package com.example.data.tables

import org.jetbrains.exposed.sql.Table

object Rescatistas : Table("rescatista") {
    val id = uuid("id_rescatista")
    val nombre = varchar("nombre", 250)
    val contrasena = varchar("contraseña", 250)

    override val primaryKey = PrimaryKey(id)
}