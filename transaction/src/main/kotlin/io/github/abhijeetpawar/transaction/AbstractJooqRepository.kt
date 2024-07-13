package io.github.abhijeetpawar.transaction

import org.jooq.DSLContext

abstract class AbstractJooqRepository() {

    protected abstract val jooqTransactionManager: JooqTransactionManager

    protected suspend fun ctx(): DSLContext = jooqTransactionManager.currentCtx()
}
