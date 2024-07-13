package io.github.abhijeetpawar.transaction

import org.jooq.DSLContext

interface JooqTransactionManager {

    suspend fun currentCtx(): DSLContext

    suspend fun <T> transactional(block: suspend () -> T): T
}
