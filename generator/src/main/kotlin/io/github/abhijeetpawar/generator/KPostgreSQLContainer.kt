package io.github.abhijeetpawar.generator

import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

class KPostgreSQLContainer(version: String) : PostgreSQLContainer<KPostgreSQLContainer>("postgres:$version") {
    fun migrate(schema: String) {
        Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .schemas(schema)
            .createSchemas(true)
            .load()
            .migrate()
    }
}
