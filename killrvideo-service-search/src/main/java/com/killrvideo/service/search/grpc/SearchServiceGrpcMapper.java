package com.killrvideo.service.search.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.utils.GrpcMappingUtils;

import killrvideo.search.SearchServiceOuterClass.*;
import killrvideo.search.SearchServiceOuterClass.SearchResultsVideoPreview;
import killrvideo.search.SearchServiceOuterClass.SearchResultsVideoPreview.Builder;
import org.springframework.stereotype.Component;


/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
public class SearchServiceGrpcMapper {
    /**
     * Mapping to generated GPRC beans (Search result special).
     */
    public SearchResultsVideoPreview maptoResultVideoPreview(Video v) {
        Builder builder = SearchResultsVideoPreview.newBuilder();
        builder.setName(v.getName());
        builder.setVideoId(GrpcMappingUtils.uuidToUuid(v.getVideoid()));
        builder.setUserId(GrpcMappingUtils.uuidToUuid(v.getUserid()));
        if (v.getPreviewImageLocation() != null)  {
            builder.setPreviewImageLocation(v.getPreviewImageLocation());
        }
        if (v.getAddedDate() != null)  {
            builder.setAddedDate(GrpcMappingUtils.instantToTimeStamp(v.getAddedDate()));
        }
        return builder.build();
    }

    public SearchVideosResponse buildSearchGrpcResponse(ResultListPage<Video> resultPage,
                                                        SearchVideosRequest initialRequest) {
        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
        builder.setQuery(initialRequest.getQuery());
        resultPage.getPagingState().ifPresent(builder::setPagingState);
        resultPage.getResults().stream()
                .map(this::maptoResultVideoPreview)
                .forEach(builder::addVideos);
        return builder.build();
    }

    public GetQuerySuggestionsResponse buildQuerySuggestionsResponse(
            Iterable<String> suggestionSet, String query
    ) {
        return GetQuerySuggestionsResponse.newBuilder()
                .setQuery(query)
                .addAllSuggestions(suggestionSet)
                .build();
    }
    
}
