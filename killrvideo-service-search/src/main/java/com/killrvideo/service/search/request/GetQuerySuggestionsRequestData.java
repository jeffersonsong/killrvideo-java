package com.killrvideo.service.search.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class GetQuerySuggestionsRequestData {
    private String query;
    private int pageSize;
}
