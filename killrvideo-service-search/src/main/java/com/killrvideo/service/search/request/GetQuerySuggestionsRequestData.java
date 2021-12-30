package com.killrvideo.service.search.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class GetQuerySuggestionsRequestData {
    private final String query;
    private final int pageSize;
}
