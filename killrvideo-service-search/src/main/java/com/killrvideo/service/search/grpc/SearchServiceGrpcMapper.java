package com.killrvideo.service.search.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.search.dto.Video;
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData;
import com.killrvideo.service.search.request.SearchVideosRequestData;
import com.killrvideo.utils.GrpcMappingUtils;

import killrvideo.search.SearchServiceOuterClass.*;
import killrvideo.search.SearchServiceOuterClass.SearchResultsVideoPreview.Builder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
public class SearchServiceGrpcMapper {

    public SearchVideosResponse buildSearchGrpcResponse(ResultListPage<Video> resultPage,
                                                        String query) {
        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
        builder.setQuery(query);
        resultPage.getPagingState().ifPresent(builder::setPagingState);
        resultPage.getResults().stream()
                .map(this::maptoResultVideoPreview)
                .forEach(builder::addVideos);
        return builder.build();
    }

    /**
     * Mapping to generated GPRC beans (Search result special).
     */
    private SearchResultsVideoPreview maptoResultVideoPreview(Video v) {
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

    public SearchVideosRequestData parseSearchVideosRequestData(SearchVideosRequest grpcReq) {
        String searchQuery = grpcReq.getQuery();
        int searchPageSize = grpcReq.getPageSize();
        Optional<String> searchPagingState = Optional.of(grpcReq.getPagingState()).filter(StringUtils::isNotBlank);

        return new SearchVideosRequestData(searchQuery, searchPageSize, searchPagingState);
    }

    public GetQuerySuggestionsResponse buildQuerySuggestionsResponse(
            Iterable<String> suggestions, String query
    ) {
        return GetQuerySuggestionsResponse.newBuilder()
                .setQuery(query)
                .addAllSuggestions(suggestions)
                .build();
    }

    public GetQuerySuggestionsRequestData parseGetQuerySuggestionsRequestData(GetQuerySuggestionsRequest grpcReq) {
        String searchQuery = grpcReq.getQuery();
        int searchPageSize = grpcReq.getPageSize();

        return new GetQuerySuggestionsRequestData(searchQuery, searchPageSize);
    }
}
