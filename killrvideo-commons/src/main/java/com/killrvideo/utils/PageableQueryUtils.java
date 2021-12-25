package com.killrvideo.utils;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.ResultListPage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PageableQueryUtils {
    private PageableQueryUtils() {
    }

    public static <T> ResultListPage<T> singleElementResult(T element) {
        ResultListPage<T> result = new ResultListPage<>();
        result.setresults(Collections.singletonList(element));
        return result;
    }

    public static BoundStatement buildStatement(
            PreparedStatement preparedStatement,
            Function<PreparedStatement, BoundStatementBuilder> function,
            int pageSize,
            Optional<String> pageState,
            ConsistencyLevel consistencyLevel) {
        BoundStatementBuilder statementBuilder = function.apply(preparedStatement);
        if (pageState.isPresent() && isNotBlank(pageState.get())) {
            statementBuilder.setPagingState(Bytes.fromHexString(pageState.get()));
        }
        statementBuilder.setPageSize(pageSize);
        statementBuilder.setConsistencyLevel(consistencyLevel);
        return statementBuilder.build();
    }

    public static <T> CompletableFuture<ResultListPage<T>> queryAsyncWithPagination(
            CqlSession session, BoundStatement boundStatement, Function<Row, T> mapper
    ) {
        CompletionStage<AsyncResultSet> resultSetFuture = session.executeAsync(boundStatement);
        return resultSetFuture.toCompletableFuture().thenApply(s -> mapToResultListPage(s, mapper));
    }

    public static <T> ResultListPage<T> mapToResultListPage(AsyncResultSet rs, Function<Row, T> mapper) {
        ResultListPage<T> result = new ResultListPage<>();
        List<T> commentList = StreamSupport.stream(rs.currentPage().spliterator(), false)
                .map(mapper).collect(Collectors.toList());
        result.setresults(commentList);
        if (rs.hasMorePages()) {
            String nextPage = Bytes.toHexString(rs.getExecutionInfo().getPagingState());
            result.setPagingState(Optional.ofNullable(nextPage));
        }
        return result;
    }
}
