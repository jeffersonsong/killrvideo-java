package com.killrvideo.utils.test;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CassandraTestUtils {
    public static <T> MappedAsyncPagingIterable<T> mockMappedAsyncPagingIterable(List<T> list) {
        MappedAsyncPagingIterable<T> iter = mock(MappedAsyncPagingIterable.class);
        when(iter.hasMorePages()).thenReturn(false);
        when(iter.currentPage()).thenReturn(list);

        return iter;
    }
}
