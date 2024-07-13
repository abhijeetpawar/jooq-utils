package io.github.abhijeetpawar.binding

import org.jooq.Binding
import org.jooq.BindingGetResultSetContext
import org.jooq.BindingGetSQLInputContext
import org.jooq.BindingGetStatementContext
import org.jooq.BindingRegisterContext
import org.jooq.BindingSQLContext
import org.jooq.BindingSetSQLOutputContext
import org.jooq.BindingSetStatementContext
import org.jooq.Converter
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import java.sql.SQLFeatureNotSupportedException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

/**
 * Binding to map `TIMESTAMP WITH TIMEZONE` to [java.time.Instant].
 *
 * Jooq by default uses [java.time.OffsetDateTime] to map `TIMESTAMP WITH TIMEZONE`, however for timestamp
 * representation [java.time.Instant] would be a more accurate choice.
 *
 * Additionally this binding uses native [java.sql.Timestamp] JDBC type, because jooq by default used to encode
 * timestamps as strings. This can result in postgres not being able to prune partitions if timestamp used as range
 * key.
 */
class OffsetDateTimeToInstantBinding : Binding<OffsetDateTime, Instant> {

    override fun converter(): Converter<OffsetDateTime, Instant> = OffsetDateTimeInstantConverter

    override fun register(ctx: BindingRegisterContext<Instant>) {
        ctx.statement().registerOutParameter(ctx.index(), Types.TIMESTAMP_WITH_TIMEZONE)
    }

    override fun sql(ctx: BindingSQLContext<Instant>) {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render()
                .visit(DSL.inline(ctx.convert(converter()).value()))
                .sql("::timestamp with timezone")
        } else {
            ctx.render().sql("?")
        }
    }

    override fun get(ctx: BindingGetResultSetContext<Instant>) {
        ctx.value(ctx.resultSet().getTimestamp(ctx.index())?.toInstant())
    }

    override fun get(ctx: BindingGetStatementContext<Instant>) {
        ctx.value(ctx.statement().getTimestamp(ctx.index())?.toInstant())
    }

    override fun set(ctx: BindingSetStatementContext<Instant>) {
        ctx.statement().setTimestamp(ctx.index(), ctx.value()?.let { Timestamp.from(it) })
    }

    /**
     * Oracle specific method
     */
    override fun get(ctx: BindingGetSQLInputContext<Instant>) {
        throw SQLFeatureNotSupportedException()
    }

    /**
     * Oracle specific method
     */
    override fun set(ct: BindingSetSQLOutputContext<Instant>) {
        throw SQLFeatureNotSupportedException()
    }

    private object OffsetDateTimeInstantConverter : Converter<OffsetDateTime, Instant> {
        override fun fromType(): Class<OffsetDateTime> = OffsetDateTime::class.java

        override fun toType(): Class<Instant> = Instant::class.java

        override fun from(dbObject: OffsetDateTime?): Instant? = dbObject?.toInstant()

        override fun to(userObject: Instant?): OffsetDateTime? = userObject?.atOffset(UTC)
    }
}
