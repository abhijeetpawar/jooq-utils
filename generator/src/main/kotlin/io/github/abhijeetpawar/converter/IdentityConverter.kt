package io.github.abhijeetpawar.converter

import kotlin.reflect.KClass

/**
 * This converter does no actual conversion but is able to trigger bindings for certain types.
 *
 * Example: Using the identity converter to generate [java.time.Instant] instead of [java.time.OffsetDateTime] fields.
 *
 * ```
 * table("event_outbox") {
 *     column("event_timestamp", IdentityConverter(Instant::class))
 * }
 * ```
 */
class IdentityConverter<T : Any>(private val clazz: KClass<T>) : Converter<T, T> {
    override val jooqType: KClass<T> = clazz
    override val resultType: KClass<T> = clazz

    override fun render(): String = """
        org.jooq.Converters.identity(${clazz.java.canonicalName}.class)
    """.trimIndent()
}
