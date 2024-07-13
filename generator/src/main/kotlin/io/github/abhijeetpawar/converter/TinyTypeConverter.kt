package io.github.abhijeetpawar.converter

import kotlin.reflect.KClass

class TinyTypeConverter<T : Any, U : Any>(
    private val fromClass: KClass<T>,
    private val toClass: KClass<U>,
) : Converter<T, U> {
    override val resultType: KClass<U> = toClass
    override val jooqType: KClass<T> = fromClass

    override fun render(): String = """
        org.jooq.Converter.ofNullable(
          ${fromClass.java.canonicalName}.class, ${toClass.java.canonicalName}.class,
          db -> new ${toClass.java.canonicalName}(db),
          v -> v.getValue()
          )
    """.trimIndent()
}
