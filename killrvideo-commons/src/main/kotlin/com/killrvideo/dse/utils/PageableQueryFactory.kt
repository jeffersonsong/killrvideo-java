package com.killrvideo.dse.utils

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.BoundStatement
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.protocol.internal.util.Bytes
import com.killrvideo.dse.dto.ResultListPage
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.function.Function

@Component
class PageableQueryFactory(private val session: CqlSession) {
    fun <ENTITY> newPageableQuery(
        query: String,
        consistencyLevel: ConsistencyLevel,
        rowMapper: Function<Row, ENTITY>
    ): PageableQuery<ENTITY> =
        PageableQueryImpl(query, session, consistencyLevel, rowMapper)

    private class PageableQueryImpl<ENTITY> constructor(
        query: String,
        private val session: CqlSession,
        private val consistencyLevel: ConsistencyLevel,
        private val rowMapper: Function<Row, ENTITY>
    ) : PageableQuery<ENTITY> {
        private val logger = KotlinLogging.logger { }
        private val preparedStatement: PreparedStatement = session.prepare(query)

        override fun queryNext(
            pageSize: Int?,
            pageState: String?,
            vararg queryParams: Any
        ): CompletableFuture<ResultListPage<ENTITY>> {
            val statement = buildStatement(pageSize, pageState, arrayOf(*queryParams))
            val result = executeAsync(statement)
            logger.debug {"Executed query ${statement.preparedStatement.query} with parameters: $queryParams"}
            return result
        }

        private fun buildStatement(
            pageSize: Int?,
            pageState: String?,
            queryParams: Array<Any>
        ): BoundStatement {
            val builder = preparedStatement.boundStatementBuilder(*queryParams)
            if (isNotBlank(pageState)) {
                builder.setPagingState(Bytes.fromHexString(pageState))
            }
            pageSize ?.let { builder.setPageSize(pageSize) }
            builder.setConsistencyLevel(consistencyLevel)
            return builder.build()
        }

        private fun executeAsync(statement: BoundStatement): CompletableFuture<ResultListPage<ENTITY>> =
            session.executeAsync(statement).toCompletableFuture()
                .thenApply { rs: AsyncResultSet -> AsyncResultSetUtils.toResultListPage(rs, rowMapper) }
    }
}
