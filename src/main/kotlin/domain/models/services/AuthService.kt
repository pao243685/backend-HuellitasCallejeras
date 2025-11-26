package com.example.domain.models.services

import com.example.config.JwtConfig
import com.example.data.tables.repositories.RescatistaRepository
import com.example.domain.models.AuthResponse
import com.example.domain.models.Rescatista
import com.example.domain.models.RescatistaLogin
import org.mindrot.jbcrypt.BCrypt

class AuthService(private val rescatistaRepository: RescatistaRepository) {

    suspend fun login(request: RescatistaLogin): AuthResponse? {
        val (rescatista, hashedPassword) = rescatistaRepository.getRescatistaByNombre(request.nombre)
            ?: return null

        if (!BCrypt.checkpw(request.contraseña, hashedPassword)) {
            return null
        }

        val token = JwtConfig.generateToken(rescatista.id, rescatista.nombre)
        return AuthResponse(token, rescatista)
    }

    suspend fun createRescatista(nombre: String, contraseña: String): Rescatista? {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(contraseña.length >= 6) { "La contraseña debe tener al menos 6 caracteres" }

        val existing = rescatistaRepository.getRescatistaByNombre(nombre)
        if (existing != null) {
            throw IllegalArgumentException("El rescatista ya existe")
        }

        val hashed = BCrypt.hashpw(contraseña, BCrypt.gensalt())
        return rescatistaRepository.createRescatista(nombre, hashed)
    }
}