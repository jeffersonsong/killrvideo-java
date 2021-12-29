package com.killrvideo.service.search.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter @AllArgsConstructor @ToString
public class SearchVideosRequestData {
    private String query;
    private int pageSize;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> pagingState;
}
