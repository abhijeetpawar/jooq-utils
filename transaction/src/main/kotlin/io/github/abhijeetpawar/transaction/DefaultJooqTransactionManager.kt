package io.github.abhijeetpawar.transaction

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class DefaultJooqTransactionManager(private val globalDslContext: DSLContext) : JooqTransactionManager {

    private val jooqExecutor by lazy {
        globalDslContext
            .configuration()
            .executorProvider().provide()
            .asCoroutineDispatcher()
    }

    override suspend fun currentCtx(): DSLContext =
        coroutineContext[TransactionKey]?.txContext ?: globalDslContext

    override suspend fun <T> transactional(block: suspend () -> T): T {
        val txContext = coroutineContext[TransactionKey]

        return if (txContext == null) {
            withNewTransaction(block)
        } else {
            block()
        }
    }

    private suspend fun <T> withNewTransaction(block: suspend () -> T): T {
        val connectionProvider = globalDslContext.configuration().connectionProvider()

        val connection = withContext(jooqExecutor) {
            connectionProvider.acquire()
                ?: throw IllegalStateException("Couldn't acquire the connection")
        }

        val autoCommit = connection.autoCommit
        withContext(jooqExecutor) {
            connection.autoCommit = false
        }
        try {
            val connectionDsl = DSL.using(connection, globalDslContext.settings())
            return withContext(JooqTransactionElement(connectionDsl)) {
                val result = block()

                withContext(jooqExecutor) {
                    connection.commit()
                }
                result
            }
        } catch (ex: Throwable) {
            withContext(jooqExecutor + NonCancellable) {
                connection.rollback()
            }
            throw ex
        } finally {
            withContext(jooqExecutor + NonCancellable) {
                connection.autoCommit = autoCommit
                connectionProvider.release(connection)
            }
        }
    }

    private class JooqTransactionElement(val txContext: DSLContext) : AbstractCoroutineContextElement(TransactionKey)

    private object TransactionKey : CoroutineContext.Key<JooqTransactionElement>
}
