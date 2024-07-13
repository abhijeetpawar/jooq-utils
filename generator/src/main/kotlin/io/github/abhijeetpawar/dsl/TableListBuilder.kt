package io.github.abhijeetpawar.dsl

import io.github.abhijeetpawar.converter.Converter

class TableListBuilder {
    private val tables: MutableList<Table> = mutableListOf()

    fun table(name: String, build: TableBuilder.() -> Unit) {
        val builder = TableBuilder()

        builder.build()

        tables.add(Table(name, builder.collectColumnData()))
    }

    fun collectTables(): List<Table> = tables

    class TableBuilder {
        private val columns: MutableList<Column> = mutableListOf()

        fun column(name: String, converter: Converter<*, *>? = null) {
            columns.add(Column(name, converter))
        }

        fun collectColumnData(): List<Column> = columns
    }
}
