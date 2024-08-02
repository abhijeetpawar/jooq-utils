dependencies {
    val v = project.extra

    api("org.jooq:jooq:${v["jooq.version"]}")
    api("org.jooq:jooq-meta:${v["jooq.version"]}")
    api("org.jooq:jooq-codegen:${v["jooq.version"]}")

    implementation("org.flywaydb:flyway-core:${v["flyway.version"]}")
    implementation("org.postgresql:postgresql:${v["postgresql-jdbc.version"]}")
    implementation("org.testcontainers:testcontainers:${v["testcontainers.version"]}")
    implementation("org.testcontainers:postgresql:${v["testcontainers.version"]}")
}
