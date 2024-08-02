# jooq-utils

![Build Status](https://github.com/abhijeetpawar/jooq-utils/actions/workflows/build.yml/badge.svg)

A Kotlin library simplifying jOOQ usage with:

- Code generation supporting [tiny types](https://darrenhobbs.com/2007/04/11/tiny-types/)
- Programmatic transactions

## Features

### 1. Code Generation

#### Usage

```kotlin
class UserRepository() {

    private companion object {
        val TABLE = Tables.USERS
    }

    suspend fun findById(userId: User.Id): User? {
        return DSLContext.selectFrom(TABLE)
            .where(TABLE.ID.eq(userId))
            .awaitFirstOrNull()?.asModel()
    }
```

#### Setup

1. Update `build.gradle.kts` -

```kotlin
ext["jooq.version"] = "x.y.z"

dependencies {
    implementation("io.github.abhijeetpawar.jooq-utils:generator:${VERSION}")
    // ...other dependencies
}

task("jooqCodeGen", JavaExec::class) {
    mainClass.set("org.company.service.JooqCodeGenKt")
    classpath = sourceSets["main"].runtimeClasspath
}

sourceSets {
    main {
        java {
            srcDirs("src/main/jooq", "src/main/kotlin", "src/main/java")
        }
    }
}
```

2. Create `JooqCodeGen.kt` that defines the table structure with any necessary type converters.

```kotlin
class JooqCodeGen

fun main(args: Array<String>) {
    JooqGenerator(
        postgresVersion = "15.2-alpine", // postgres:15.2-alpine
        schema = "public"
    ).generate {
        usersTable()
    }
}

fun TableListBuilder.usersTable() {
    table("users") {
        column("id", TinyTypeConverter(UUID::class, User.Id::class))
        column("first_name", TinyTypeConverter(String::class, User.FirstName::class))
        column("last_name", TinyTypeConverter(String::class, User.LastName::class))
        // ... other columns    
    }
}
```

3. Generate jOOQ sources:

```bash
./gradlew jooqCodeGen
```

`NOTE:`
- Make sure you have docker installed
- Migrations are present under resources/db/migration

### 2. Transactions

#### Usage

```kotlin
class UserRepository(override val jooqTransactionManager: JooqTransactionManager) : AbstractJooqRepository() {

    companion object {
        val TABLE = Tables.USERS
    }

    suspend fun findOne(userId: User.Id): User? {
        return ctx().selectFrom(TABLE)
            .where(TABLE.ID.eq(userId))
            .awaitFirstOrNull()?.map { it.toDomainModel() }
    }
}
```

```kotlin
class UserService(val userRepository: UserRepository, val transactionManager: JooqTransactionManager) {
    suspend fun createUser(): User {
        val checkDuplicate = http()

        val user = User(...)
        transactionManager.transactional {
            userPreferences.save(preferences)
            userRepository.save(user)
        }
    }
}
```

#### Setup

1. Add to `build.gradle.kts`

```kotlin
implementation("io.github.abhijeetpawar.jooq-utils:transaction:${VERSION}")
```

2. Setup transaction manager

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

## Roadmap

1. Add usage examples & improve test coverage
2. Update to JDK 21
3. Support other databases (e.g., MySQL)

## Contributing

- Report issues via GitHub issue tracker
- Submit pull requests from a forked repository
- Ensure code is tested and follows existing style
- Update documentation for significant changes

## Development Setup

1. Clone the repository
2. Install Docker (required for tests)
3. Run ./gradlew build to install dependencies
4. Execute tests with ./gradlew test

`NOTE:` This project follows [semantic versioning](https://semver.org/) format.

