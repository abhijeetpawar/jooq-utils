rootProject.name = "jooq-utils"

include("generator", "transaction")

project(":generator").name = "generator"
project(":transaction").name = "transaction"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/abhijeetpawar/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
