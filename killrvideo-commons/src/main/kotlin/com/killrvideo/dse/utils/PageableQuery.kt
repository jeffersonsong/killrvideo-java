package com.killrvideo.dse.utils

import com.killrvideo.dse.dto.ResultListPage
import java.util.concurrent.CompletableFuture

interface PageableQuery<ENTITY> {
    fun queryNext(
        pageSize: Int?,
        pageState: String?,
        vararg queryParams: Any
    ): CompletableFuture<ResultListPage<ENTITY>>
}
