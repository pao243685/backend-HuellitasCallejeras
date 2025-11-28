package com.example.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.models.TokenClaims
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.util.UUID

fun Application.configureSecurity() {
    val secret = System.getenv("JWT_SECRET") ?: "huellitas-secret-key"
    val issuer = "huellitas-callejeras-api"
    val algorithm = Algorithm.HMAC256(secret)

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val rescatistaId = credential.payload.getClaim("rescatistaId").asString()
                val nombre = credential.payload.getClaim("nombre").asString()

                if (rescatistaId != null && nombre != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}