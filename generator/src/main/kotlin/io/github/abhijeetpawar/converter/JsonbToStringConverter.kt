package io.github.abhijeetpawar.converter

import org.jooq.JSONB
import kotlin.reflect.KClass

class JsonbToStringConverter : Converter<JSONB, String> {
    override val resultType: KClass<String> = String::class
    override val jooqType: KClass<JSONB> = JSONB::class

    override fun render(): String = """
        org.jooq.Converter.ofNullable(
          ${jooqType.java.canonicalName}.class, ${resultType.java.canonicalName}.class,
          db -> db.data(),
          v -> ${jooqType.java.canonicalName}.valueOf(v)
          )
    """.trimIndent()
}
