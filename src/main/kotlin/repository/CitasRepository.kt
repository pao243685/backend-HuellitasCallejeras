package com.example.repository

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.TablaCitas
import com.example.models.Cita
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class CitasRepository {

    suspend fun getAllCitas(): List<Cita> = dbQuery{
        TablaCitas.selectAll().map{
            Cita(
                idCitas = it[TablaCitas.id],
                fechaRealizacion = it[TablaCitas.fecha_realizacion] as LocalDate,
                fechaCita = it[TablaCitas.fecha_cita] as LocalDate,
                motivo = it[TablaCitas.motivo],
                lugar = it[TablaCitas.lugar],
                idAnimalito = it[TablaCitas.animal_id]
            )
        }
    }

    suspend fun insert(cita: Cita) = dbQuery {
        TablaCitas.insert {
            it[id] = cita.idCitas
            it[fecha_realizacion] = cita.fechaRealizacion
            it[fecha_cita] = cita.fechaCita
            it[motivo] = cita.motivo
            it[lugar] = cita.lugar
            it[animal_id] = cita.idAnimalito
        }
    }
}