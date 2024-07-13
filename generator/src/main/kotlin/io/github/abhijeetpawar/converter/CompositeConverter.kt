package io.github.abhijeetpawar.converter

import kotlin.reflect.KClass

class CompositeConverter<T : Any, U : Any, X : Any>(
    private val converter1: Converter<T, X>,
    private val converter2: Converter<X, U>
) : Converter<T, U> {
    override val resultType: KClass<U> = converter2.resultType
    override val jooqType: KClass<T> = converter1.jooqType

    override fun render(): String = """
        org.jooq.Converters.of(${converter1.render()}, ${converter2.render()})
    """.trimIndent()
}
