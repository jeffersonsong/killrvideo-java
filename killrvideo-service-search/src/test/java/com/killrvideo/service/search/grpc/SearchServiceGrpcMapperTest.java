package com.killrvideo.service.search.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData;
import com.killrvideo.service.search.request.SearchVideosRequestData;
import killrvideo.search.SearchServiceOuterClass.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class SearchServiceGrpcMapperTest {
    private final SearchServiceGrpcMapper mapper = new SearchServiceGrpcMapper();

    @Test
    public void testBuildSearchGrpcResponse() {
        Video v = new Video();
        v.setUserid(UUID.randomUUID());
        v.setVideoid(UUID.randomUUID());
        v.setName("Game");
        v.setPreviewImageLocation("url");
        v.setAddedDate(Instant.now());

        ResultListPage<Video> resultPage = new ResultListPage<>(singletonList(v), Optional.of("paging state"));

        SearchVideosResponse response = mapper.buildSearchGrpcResponse(resultPage, "query");
        assertEquals("query", response.getQuery());
        assertEquals(resultPage.getPagingState().get(), response.getPagingState());
        assertEquals(1, response.getVideosCount());
    }

    @Test
    public void testParseSearchVideosRequestData() {
        SearchVideosRequest request = SearchVideosRequest.newBuilder()
                .setQuery("Linux")
                .setPageSize(5)
                .setPagingState("Paging state")
                .build();

        SearchVideosRequestData pojo = mapper.parseSearchVideosRequestData(request);
        assertEquals(request.getQuery(), pojo.getQuery());
        assertEquals(request.getPageSize(), pojo.getPageSize());
        assertEquals(request.getPagingState(), pojo.getPagingState().get());
    }

    @Test
    public void testBuildQuerySuggestionsResponse() {
        List<String> suggestions = asList("suggestion1", "suggestion2");
        String query = "query";
        GetQuerySuggestionsResponse response = mapper.buildQuerySuggestionsResponse(suggestions, query);
        assertEquals(query, response.getQuery());
        assertEquals(2, response.getSuggestionsCount());
    }

    @Test
    public void testParseGetQuerySuggestionsRequestData() {
        GetQuerySuggestionsRequest request = GetQuerySuggestionsRequest.newBuilder()
                .setQuery("query")
                .setPageSize(2)
                .build();

        GetQuerySuggestionsRequestData pojo = mapper.parseGetQuerySuggestionsRequestData(request);
        assertEquals("query", pojo.getQuery());
        assertEquals(2, pojo.getPageSize());
    }
}
