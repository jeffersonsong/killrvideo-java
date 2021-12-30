package com.killrvideo.dse.utils;

import com.killrvideo.dse.dto.ResultListPage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface PageableQuery<ENTITY> {
    CompletableFuture<ResultListPage<ENTITY>> queryNext(
            Optional<Integer> pageSize,
            Optional<String> pageState,
            Object... queryParams);
}
