package io.github.abhijeetpawar.converter

import kotlin.reflect.KClass

class EnumConverter<T : Any, U : Enum<U>>(
    private val fromClass: KClass<T>,
    private val toClass: KClass<U>,
) : Converter<T, U> {
    override val resultType: KClass<U> = toClass
    override val jooqType: KClass<T> = fromClass

    override fun render(): String = """
        new org.jooq.impl.EnumConverter(${fromClass.java.canonicalName}.class, ${toClass.java.canonicalName}.class)
    """.trimIndent()
}
