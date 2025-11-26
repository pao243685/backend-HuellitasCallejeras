package com.example.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TratamientoMedicamento : Table("tratamiento_medicamento") {
    val tratamientoId = uuid("tratamiento_id").references(Tratamientos.id)
    val medicamentoId = uuid("medicamento_id").references(Medicamentos.id)
    val dosis = float("dosis")
    val fechaConclusion = timestamp("fecha_conclusion").nullable()
    val repeticion = float("repeticion")

    override val primaryKey = PrimaryKey(tratamientoId, medicamentoId)
}