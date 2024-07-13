rootProject.name = "jooq-utils"

include("generator", "transaction")

project(":generator").name = "jooq-utils-generator"
project(":transaction").name = "jooq-utils-transaction"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
