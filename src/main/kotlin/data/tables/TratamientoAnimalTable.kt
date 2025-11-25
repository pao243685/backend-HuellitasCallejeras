package com.example.data.tables

import org.jetbrains.exposed.sql.Table

object TratamientoAnimalito : Table("tratamiento_animalito") {
    val animalitoId = integer("animalito_id").references(Animalitos.id)
    val tratamientoId = integer("tratamiento_id").references(Tratamientos.id)

    override val primaryKey = PrimaryKey(animalitoId, tratamientoId)
}

