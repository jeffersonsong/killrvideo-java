package com.killrvideo.dse.utils

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import java.util.concurrent.ExecutionException

object MappedAsyncPagingIterableExtensions {
    fun <T> MappedAsyncPagingIterable<T>.all(): List<T> {
        val result = mutableListOf<T>()

        var iter: MappedAsyncPagingIterable<T> = this

        result.addAll(iter.currentPage())
        while (iter.hasMorePages()) {
            try {
                iter = iter.fetchNextPage().toCompletableFuture().get()
                result.addAll(iter.currentPage())
            } catch (ex: InterruptedException) {
                break
            } catch (ex: ExecutionException) {
                break
            }
        }

        return result
    }
}