dependencies {
    val v = project.extra

    api("org.jooq:jooq:${v["jooq.version"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${v["kotlin-coroutines.version"]}")
}
