package com.killrvideo.utils.test;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CassandraTestUtils {
    @SuppressWarnings("unchecked")
    public static <T> MappedAsyncPagingIterable<T> mockMappedAsyncPagingIterable(List<T> list) {
        MappedAsyncPagingIterable<T> iter = mock(MappedAsyncPagingIterable.class);
        when(iter.hasMorePages()).thenReturn(false);
        when(iter.currentPage()).thenReturn(list);

        return iter;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> PageableQueryFactory mockPageableQueryFactory(PageableQuery<T>... queries) {
        PageableQueryFactory pageableQueryFactory = mock(PageableQueryFactory.class);

        switch (queries.length) {
            case 0 :
                break;
            case 1:
                when(pageableQueryFactory.newPageableQuery(any(), any(), (Function<Row, T>) any()))
                        .thenReturn(queries[0]);
                break;
            default:
                when(pageableQueryFactory.newPageableQuery(any(), any(), (Function<Row, T>) any()))
                        .thenReturn(queries[0], Arrays.copyOfRange(queries, 1, queries.length));
        }

        return pageableQueryFactory;
    }
}
