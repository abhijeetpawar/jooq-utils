# jooq-utils

A **library** to simplify the setup and usage of: 
* `jooq` code generation with domain types.
* `jooq` transaction management.

## Jooq Code Gen

Changes for `build.gradle.kts` -

Add a dependency of the library to your build script.

```kotlin
// for database mapping generator code
implementation("io.github.abhijeetpawar.jooq:jooq-utils-generator:${VERSION}")

// for transaction management code
implementation("io.github.abhijeetpawar.jooq:jooq-utils-transaction:${VERSION}")
```

It is recommended to set the `jooq` version for your project like so,
```kotlin
ext["jooq.version"] = "x.y.z"
```

Add the following config which creates a task execute to generate jooq code in `src/main/jooq/persistence` and includes this folder in the src set for compilation.

```kotlin
task("jooqCodeGen", JavaExec::class) {
    mainClass.set("org.company.service.JooqCodeGenKt")
    classpath = sourceSets["main"].runtimeClasspath
}

sourceSets {
    main {
        java {
            srcDirs("src/main/jooq", "src/main/kotlin")
        }
    }
}
```

### Kotlin

Create a kotlin file as follows that defines the table structure with any necessary type converters.

```kotlin
class JooqCodeGen {

}

fun main(args: Array<String>) {
    JooqGenerator().generate {
        table("users") {
            column("id")
            column("name")
        }
        table("events") {
            column("create_time", TinyTypeConverter(Instant::class, CreateTime::class))
            column("id", TinyTypeConverter(UUID::class, Id::class))
            column("type", EnumConverter(String::class, EventType::class))
            column("payload", CompositeConverter(JsonbToStringConverter(), TinyTypeConverter(String::class, EventPayload::class)))
            column("published", TinyTypeConverter(Boolean::class, Published::class))
        }
    }
}
```

**Execute bash command to generate jooq sources -**
```bash
$ ./gradlew jooqCodeGen
```

## Transactions

Repositories can be implemented like so,

```kotlin
class UserRepository(override val jooqTransactionManager: JooqTransactionManager) : AbstractJooqRepository() {

    companion object {
        val TABLE = USERS
    }

    suspend fun findOne(userId: UUID): User? {
        return ctx().selectFrom(TABLE)
            .where(TABLE.ID.eq(userId))
            .await()
            .map { it.asModel() }.firstOrNull()
    }
}
```

where `JooqTransactionManager` can simply be setup as,

```kotlin
class Config {
    
    @Bean
    fun dslContext(dataSource: DataSource): DSLContext {
        return DefaultDSLContext(dataSource, SQLDialect.POSTGRES)
    }

    @Bean
    fun jooqTransactionManager(dslContext: DSLContext): JooqTransactionManager {
        return DefaultJooqTransactionManager(dslContext)
    }
}
```

For executing transaction, add a dependency to `JooqTransactionManager`in the service, and use the `transactional { }` to invoke transactions programatically.


## Contributing

Your contributions to improve the `jooq-utils` project are welcome! Here's how you can contribute:

### Reporting Issues

- Use the GitHub issue tracker to report bugs or suggest features.
- Before creating a new issue, please check if a similar issue already exists.
- Provide as much context as possible when reporting bugs. Include your OS, Java version, jOOQ version, and steps to reproduce the issue.

### Pull Requests

1. Fork the repository and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. Ensure your code follows the existing style to maintain consistency.
4. Update the documentation if you've made changes that affect how the library is used.
5. Make sure your code lints and passes all tests.
6. Issue your pull request!

### Development Setup

1. Clone the repository: `git clone https://github.com/yourusername/your-repo-name.git`
2. Tests depend on docker, so make sure you have docker installed on your machine.
2. Install dependencies: `./gradlew build` (or `mvn install` if using Maven)
3. Run tests: `./gradlew test` (or `mvn test`)


`NOTE:` This project follows [semantic versioning](https://semver.org/) format.
Patch version updates must be backward compatible.
