package io.github.abhijeetpawar.converter

import kotlin.reflect.KClass

interface Converter<T : Any, U : Any> {
    val resultType: KClass<U>
    val jooqType: KClass<T>

    fun render(): String
}
