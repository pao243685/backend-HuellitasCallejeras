package com.example

import com.example.Routing.configureRouting
import com.example.database.DatabaseFactory
import com.example.plugins.configureSerialization
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()

    configureRouting()
}

