package com.example


import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("FlexibleInstant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.ISO_INSTANT.format(value))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val dateString = decoder.decodeString()
        return try {
            // Intentar parsear como ISO Instant (con Z)
            Instant.parse(dateString)
        } catch (e: Exception) {
            try {
                // Intentar parsear como LocalDateTime (sin Z)
                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(ZoneOffset.UTC)
            } catch (e: Exception) {
                try {
                    // Intentar parsear como LocalDate (solo fecha)
                    LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Formato de fecha no válido: $dateString. Use formato: 2024-01-15T00:00:00Z")
                }
            }
        }
    }
}