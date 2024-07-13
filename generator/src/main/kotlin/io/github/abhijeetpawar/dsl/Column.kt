package io.github.abhijeetpawar.dsl

import io.github.abhijeetpawar.converter.Converter

data class Column(
    val name: String,
    val converter: Converter<*, *>?
)
