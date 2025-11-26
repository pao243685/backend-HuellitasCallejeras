package com.example.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Animalitos : Table("animalito") {
    val id = uuid("id_animalito").clientDefault { UUID.randomUUID() }
    val nombre = varchar("nombre", 250)
    val peso = float("peso")
    val raza = varchar("raza", 250).nullable()
    val sexo = varchar("sexo", 250)
    val edad = integer("edad")
    val especie = varchar("especie", 250)
    val estado = varchar("estado", 250)
    val fechaSalida = timestamp("fecha_salida").nullable()
    val urlImage = varchar("urlimagen", 250)

    override val primaryKey = PrimaryKey(id)
}

object Rescates : Table("rescate") {
    val id = uuid("id_rescate").clientDefault { UUID.randomUUID() }
    val fechaIngreso = timestamp("fecha_ingreso")
    val lugar = varchar("lugar", 250)
    val descripcion = varchar("descripcion", 250)
    val animalitoId = uuid("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}

object Tratamientos : Table("tratamiento") {
    val id = uuid("id_tratamiento").clientDefault { UUID.randomUUID() }
    val fechaInicio = timestamp("fecha_inicio")
    val receta = varchar("receta", 250)

    override val primaryKey = PrimaryKey(id)
}

object Medicamentos : Table("medicamentos") {
    val id = uuid("id_medicamento").clientDefault { UUID.randomUUID() }
    val nombre = varchar("nombre", 250)

    override val primaryKey = PrimaryKey(id)
}

object TratamientoMedicamento : Table("tratamiento_medicamento") {
    val tratamientoId = uuid("tratamiento_id").references(Tratamientos.id)
    val medicamentoId = uuid("medicamento_id").references(Medicamentos.id)
    val dosis = float("dosis")
    val fechaConclusion = timestamp("fecha_conclusion").nullable()
    val repeticion = float("repeticion")

    override val primaryKey = PrimaryKey(tratamientoId, medicamentoId)
}

object TratamientoAnimalito : Table("tratamiento_animalito") {
    val animalitoId = uuid("animalito_id").references(Animalitos.id)
    val tratamientoId = uuid("tratamiento_id").references(Tratamientos.id)

    override val primaryKey = PrimaryKey(animalitoId, tratamientoId)
}

object Citas : Table("citas") {
    val id = uuid("id_citas").clientDefault { UUID.randomUUID() }
    val fechaRealizacion = timestamp("fecha_realizacion")
    val fechaCita = timestamp("fecha_cita")
    val titulo = varchar("titulo", 250)
    val motivo = varchar("motivo", 250)
    val lugar = varchar("lugar", 250)
    val animalitoId = uuid("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}