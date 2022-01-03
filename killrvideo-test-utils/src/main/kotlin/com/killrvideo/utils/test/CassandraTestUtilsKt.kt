package com.killrvideo.utils.test

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import io.mockk.every
import io.mockk.mockk
import java.util.function.Function

// TODO - Remove JvmStatic, convert to mockk
object CassandraTestUtilsKt {
    @JvmStatic
    fun <T> mockMappedAsyncPagingIterable(list: List<T>): MappedAsyncPagingIterable<T> {
        val iter: MappedAsyncPagingIterable<T> = mockk<MappedAsyncPagingIterable<T>>()
        every {iter.hasMorePages()} returns false
        every { iter.currentPage() } returns list
        return iter
    }

    @JvmStatic
    @SafeVarargs
    fun <T> mockPageableQueryFactory(vararg queries: PageableQuery<T>): PageableQueryFactory {
        val pageableQueryFactory = mockk<PageableQueryFactory>()
        when (queries.size) {
            0 -> {}
            1 -> every {
                pageableQueryFactory.newPageableQuery(any(), any(), any<Function<Row, T>>())
            } returns queries[0]
            else -> every {
                pageableQueryFactory.newPageableQuery(any(), any(), any<Function<Row, T>>())
            } returnsMany queries.toList()
        }
        return pageableQueryFactory
    }
}
