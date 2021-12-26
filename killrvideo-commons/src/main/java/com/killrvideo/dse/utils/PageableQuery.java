package com.killrvideo.dse.utils;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.ResultListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PageableQuery<ENTITY> {
    private static Logger LOGGER = LoggerFactory.getLogger(PageableQuery.class);

    private CqlSession session;
    private PreparedStatement preparedStatement;
    private ConsistencyLevel consistencyLevel;
    private Function<Row, ENTITY> rowMapper;

    public PageableQuery(String query,
                         CqlSession session,
                         ConsistencyLevel consistencyLevel,
                         Function<Row, ENTITY> rowMapper
    ) {
        this.session = session;
        this.preparedStatement = session.prepare(query);
        this.consistencyLevel = consistencyLevel;
        this.rowMapper = rowMapper;
    }

    public CompletableFuture<ResultListPage<ENTITY>> queryNext(
            Optional<Integer> pageSize,
            Optional<String> pageState,
            Object... queryParams) {
        BoundStatement statement = buildStatement(queryParams, pageSize, pageState);
        CompletableFuture<ResultListPage<ENTITY>> result = executeAsync(statement);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executed query {} with parameters: {}", statement.getPreparedStatement().getQuery(), queryParams);
        }
        return result;
    }

    private BoundStatement buildStatement(
            Object[] queryParams,
            Optional<Integer> pageSize,
            Optional<String> pageState) {
        BoundStatementBuilder builder = preparedStatement.boundStatementBuilder(queryParams);
        if (pageState.isPresent() && isNotBlank(pageState.get())) {
            builder.setPagingState(Bytes.fromHexString(pageState.get()));
        }
        pageSize.ifPresent(builder::setPageSize);
        builder.setConsistencyLevel(consistencyLevel);
        return builder.build();
    }

    private CompletableFuture<ResultListPage<ENTITY>> executeAsync(BoundStatement statement) {
        return this.session.executeAsync(statement).toCompletableFuture()
                .thenApply(rs -> ResultListPage.from(rs, rowMapper));
    }
}
