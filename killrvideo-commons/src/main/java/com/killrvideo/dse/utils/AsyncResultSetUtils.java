package com.killrvideo.dse.utils;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.ResultListPage;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AsyncResultSetUtils {
    private AsyncResultSetUtils() {
    }

    public static <T> ResultListPage<T> toResultListPage(AsyncResultSet rs, Function<Row, T> mapper) {
        List<T> list = StreamSupport.stream(rs.currentPage().spliterator(), false)
                .map(mapper).collect(Collectors.toList());
        Optional<String> nextPage = getNextPage(rs);
        return new ResultListPage<>(list, nextPage);
    }

    private static Optional<String> getNextPage(AsyncResultSet rs) {
        if (rs.hasMorePages() && rs.getExecutionInfo().getPagingState() != null) {
            return Optional.ofNullable(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
        } else {
            return Optional.empty();
        }
    }
}
