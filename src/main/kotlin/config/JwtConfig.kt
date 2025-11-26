package com.example.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date
import java.util.UUID


object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "huellitas-secret-key"
    private val issuer = "huellitas-callejeras-api"
    private val validityInMs = 36_000_00 * 24 * 30
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(rescatistaId: UUID, nombre: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("rescatistaId", rescatistaId.toString())
            .withClaim("nombre", nombre)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT {
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        return verifier.verify(token)
    }
}