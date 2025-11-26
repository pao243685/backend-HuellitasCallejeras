package com.example.domain.models

import com.example.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Rescatista(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val nombre: String
)

@Serializable
data class RescatistaLogin(
    val nombre: String,
    val contraseña: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val rescatista: Rescatista
)

@Serializable
data class TokenClaims(
    @Serializable(with = UUIDSerializer::class)
    val rescatistaId: UUID,
    val nombre: String
)