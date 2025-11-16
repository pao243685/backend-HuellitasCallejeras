package com.example.repository

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.TablaAnimal
import com.example.database.TablaAnimal.edad
import com.example.database.TablaAnimal.especie
import com.example.database.TablaAnimal.estado
import com.example.database.TablaAnimal.fechaSalida
import com.example.database.TablaAnimal.nombre
import com.example.database.TablaAnimal.peso
import com.example.database.TablaAnimal.raza
import com.example.database.TablaAnimal.sexo
import com.example.models.Animal
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class AnimalRepositorio {

    suspend fun getAll(): List<Animal> = dbQuery {
        TablaAnimal.selectAll().map {
            Animal(
                idAnimal = it[TablaAnimal.id],
                nombre = it[TablaAnimal.nombre],
                especie = it[TablaAnimal.especie],
                raza = it[TablaAnimal.raza],
                peso = it[TablaAnimal.peso],
                edad = it[TablaAnimal.edad],
                fechaSalida = it[TablaAnimal.fechaSalida]?.toKotlinLocalDate(),
                estado = it[TablaAnimal.estado],
                sexo = it[TablaAnimal.sexo],
            )
        }
    }

    suspend fun insert(animal: Animal) = dbQuery {
        TablaAnimal.insert {
            it[id] = animal.idAnimal
            it[nombre] = animal.nombre
            it[especie] = animal.especie
            it[raza] = animal.raza
            it[sexo] = animal.sexo
            it[peso] = animal.peso
            it[edad] = animal.edad
            it[fechaSalida] = animal.fechaSalida?.toJavaLocalDate()
            it[estado] = animal.estado
        }
    }
}