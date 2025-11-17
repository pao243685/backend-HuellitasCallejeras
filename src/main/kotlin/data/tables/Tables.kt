package com.example.tables

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

object Rescates : Table("rescate") {
    val id = integer("id_rescate").autoIncrement()
    val fechaIngreso = timestamp("fecha_ingreso")
    val lugar = varchar("lugar", 250)
    val descripcion = varchar("descripcion", 250)
    val animalitoId = integer("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}

object Tratamientos : Table("tratamiento") {
    val id = integer("id_tratamiento").autoIncrement()
    val fechaInicio = timestamp("fecha_inicio")
    val receta = varchar("receta", 250)

    override val primaryKey = PrimaryKey(id)
}

object Medicamentos : Table("medicamentos") {
    val id = integer("id_medicamento").autoIncrement()
    val nombre = varchar("nombre", 250)

    override val primaryKey = PrimaryKey(id)
}

object TratamientoMedicamento : Table("tratamiento_medicamento") {
    val tratamientoId = integer("tratamiento_id").references(Tratamientos.id)
    val medicamentoId = integer("medicamento_id").references(Medicamentos.id)
    val dosis = float("dosis")
    val fechaConclusion = timestamp("fecha_conclusion").nullable()
    val repeticion = float("repeticion")

    override val primaryKey = PrimaryKey(tratamientoId, medicamentoId)
}

object TratamientoAnimalito : Table("tratamiento_animalito") {
    val animalitoId = integer("animalito_id").references(Animalitos.id)
    val tratamientoId = integer("tratamiento_id").references(Tratamientos.id)

    override val primaryKey = PrimaryKey(animalitoId, tratamientoId)
}

object Citas : Table("citas") {
    val id = integer("id_citas").autoIncrement()
    val fechaRealizacion = timestamp("fecha_realizacion")
    val fechaCita = timestamp("fecha_cita")
    val motivo = varchar("motivo", 250)
    val lugar = varchar("lugar", 250)
    val animalitoId = integer("animalito_id").references(Animalitos.id)

    override val primaryKey = PrimaryKey(id)
}