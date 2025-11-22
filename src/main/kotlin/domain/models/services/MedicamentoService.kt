package com.example.domain.models.services

import com.example.data.tables.repositories.MedicamentoRepository
import com.example.domain.models.Medicamento
import com.example.domain.models.MedicamentoRequest
import java.util.UUID

class MedicamentoService(private val repository: MedicamentoRepository) {

    suspend fun getAllMedicamentos() = repository.getAllMedicamentos()

    suspend fun getMedicamentoById(id: UUID) = repository.getMedicamentoById(id)

    suspend fun createMedicamento(request: MedicamentoRequest): Medicamento? {
        require(request.nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        return repository.createMedicamento(request)
    }

    suspend fun updateMedicamento(id: UUID, nombre: String): Boolean {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        return repository.updateMedicamento(id, nombre)
    }

    suspend fun deleteMedicamento(id: UUID) = repository.deleteMedicamento(id)
}