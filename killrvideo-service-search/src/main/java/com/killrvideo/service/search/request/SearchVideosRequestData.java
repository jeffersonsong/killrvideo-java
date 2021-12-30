package com.killrvideo.service.search.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter @AllArgsConstructor @ToString
public class SearchVideosRequestData {
    private final String query;
    private final int pageSize;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> pagingState;
}
