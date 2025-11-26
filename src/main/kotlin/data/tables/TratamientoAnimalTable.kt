package com.example.data.tables

import org.jetbrains.exposed.sql.Table

object TratamientoAnimal : Table("tratamiento_animal") {
    val animalId = uuid("animal_id").references(animal.id)
    val tratamientoId = uuid("tratamiento_id").references(Tratamientos.id)

    override val primaryKey = PrimaryKey(animalId, tratamientoId)
}