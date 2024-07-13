package io.github.abhijeetpawar.generator

import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import io.github.abhijeetpawar.binding.OffsetDateTimeToInstantBinding
import io.github.abhijeetpawar.dsl.Column
import io.github.abhijeetpawar.dsl.Table
import io.github.abhijeetpawar.dsl.TableListBuilder
import java.time.Instant

class JooqGenerator(
    private val schema: String,

    /**
     * Version suffix for postgres docker image.
     *
     * Eg: "13.4-alpine" for `postgres:13.4-alpine`
     */
    private val postgresVersion: String = "13.4-alpine",
    private val db: String = "test",
    private val username: String = "test",
    private val password: String = "test",
    private val targetPackageName: String = "persistence",
    private val targetDirectory: String = "src/main/jooq",

    @Deprecated("Prefer to use kotlin data classes instead of auto-generated pojos")
    private val generatePojos: Boolean = false
) {

    fun generate(tableBuilder: TableListBuilder.() -> Unit) {
        val postgres = KPostgreSQLContainer(postgresVersion)
            .withDatabaseName(db)
            .withUsername(username)
            .withPassword(password)

        postgres.use { pg ->
            pg.start()
            pg.migrate(schema)

            val tables = TableListBuilder().apply { tableBuilder() }.collectTables()
            val config = Configuration()
                .withJdbc(pg.jdbc)
                .withGenerator(
                    Generator()
                        .withGenerate(generate)
                        .withDatabase(configureDatabase(tables))
                        .withTarget(target)
                )

            GenerationTool.generate(config)
        }
    }

    private val KPostgreSQLContainer.jdbc
        get() = Jdbc()
            .withDriver("org.postgresql.Driver")
            .withUrl(jdbcUrl)
            .withUser(username)
            .withPassword(password)

    private val generate
        get() = Generate()
            .withRoutines(false)
            .withRecords(true)
            .withFluentSetters(true)
            .withJavaTimeTypes(true)
            .withPojos(generatePojos)

    private val target
        get() = Target()
            .withPackageName(targetPackageName)
            .withDirectory(targetDirectory)

    private fun configureDatabase(tables: List<Table>): Database =
        Database()
            .withName("org.jooq.meta.postgres.PostgresDatabase")
            .withInputSchema(schema)
            .withIncludes(tables.joinToString(separator = "|") { it.name })
            .withForcedTypes(tables.flatMap { it.toForcedTypes() })

    private fun Table.toForcedTypes(): List<ForcedType> =
        columns.mapNotNull { it.toForcedType(name) }

    private fun Column.toForcedType(tableName: String): ForcedType? {
        return if (this.converter == null) {
            null
        } else {
            ForcedType()
                .withUserType(converter.resultType.java.canonicalName)
                .withConverter(converter.render())
                .withIncludeExpression("$schema.$tableName.$name")
                .also {
                    if (converter.jooqType == Instant::class) {
                        it.withBinding(OffsetDateTimeToInstantBinding::class.java.canonicalName)
                    }
                }
        }
    }
}
