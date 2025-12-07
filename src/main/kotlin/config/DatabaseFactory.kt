package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {

    fun init() {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
//            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://34.195.100.95:5432/huellitas_callejeras"
            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://127.0.0.1:5432/huellitas_callejeras5"
            username = System.getenv("DB_USER") ?: "postgres"
            password = System.getenv("DB_PASSWORD") ?: "Perlita1610"
//            password = System.getenv("DB_PASSWORD") ?: "123456"
            //password = System.getenv("DB_PASSWORD") ?: "huellitas123"
//            password = System.getenv("DB_PASSWORD") ?: "2006"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(config))
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}