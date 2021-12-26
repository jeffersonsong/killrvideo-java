package com.killrvideo.dse.utils;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MappedAsyncPagingIterableUtils {
    private MappedAsyncPagingIterableUtils() {}

    public static <T> List<T> toList(MappedAsyncPagingIterable<T> pagingIterable) {
        List<T> result = new ArrayList<>();

        MappedAsyncPagingIterable<T> iter = pagingIterable;

        iter.currentPage().forEach(result::add);
        while (iter.hasMorePages()) {
            try {
                iter = iter.fetchNextPage().toCompletableFuture().get();
                iter.currentPage().forEach(result::add);
            } catch (InterruptedException | ExecutionException ex) {
                break;
            }
        }

        return result;
    }
}
