package com.killrvideo.service.search.grpc;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.search.repository.SearchRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.killrvideo.dse.dto.ResultListPage;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceGrpc.SearchServiceImplBase;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;

import javax.inject.Inject;

/**
 * Service SEARCG.
 *
 * @author DataStax advocates Team
 */
@Service
public class SearchServiceGrpc extends SearchServiceImplBase {

    /** Loger for that class. */
    private static Logger LOGGER = LoggerFactory.getLogger(SearchServiceGrpc.class);
    
    @Value("${killrvideo.discovery.services.search : SearchService}")
    private String serviceKey;
   
    @Inject
    private SearchRepository dseSearchDao;
    @Inject
    private SearchServiceGrpcValidator validator;
    @Inject
    private SearchServiceGrpcMapper mapper;
    
    /** {@inheritDoc} */
    @Override
    public void searchVideos(SearchVideosRequest grpcReq, StreamObserver<SearchVideosResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_SearchVideos(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        String           searchQuery = grpcReq.getQuery();
        int              searchPageSize = grpcReq.getPageSize();
        Optional<String> searchPagingState = Optional.ofNullable(grpcReq.getPagingState()).filter(StringUtils::isNotBlank);
        
        // Map Result back to GRPC
        dseSearchDao
            .searchVideosAsync(searchQuery, searchPageSize,searchPagingState)
            .whenComplete((resultPage, error) -> {
              if (error == null) {
                  traceSuccess("searchVideos", starts);
                  grpcResObserver.onNext(buildSearchGrpcResponse(resultPage, grpcReq));
                  grpcResObserver.onCompleted();
                  
               } else {
                  traceError("searchVideos", starts, error);
                  grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
               }
        });
    }
    
    private SearchVideosResponse buildSearchGrpcResponse(ResultListPage<Video> resultPage, SearchVideosRequest initialRequest) {
        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
        builder.setQuery(initialRequest.getQuery());
        resultPage.getPagingState().ifPresent(builder::setPagingState);
        resultPage.getResults().stream()
                  .map(mapper::maptoResultVideoPreview)
                  .forEach(builder::addVideos);
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest grpcReq, StreamObserver<GetQuerySuggestionsResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_GetQuerySuggestions(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        String           searchQuery = grpcReq.getQuery();
        int              searchPageSize = grpcReq.getPageSize();
        
        // Invoke Dao (Async)
        CompletableFuture<TreeSet<String>> futureDao = 
                dseSearchDao.getQuerySuggestionsAsync(searchQuery, searchPageSize);
        
        // Mapping back to GRPC beans
        futureDao.whenComplete((suggestionSet, error) -> {
          if (error == null) {
              traceSuccess("getQuerySuggestions", starts);
              grpcResObserver.onNext(buildQuerySuggestionsResponse(grpcReq.getQuery(), suggestionSet));
              grpcResObserver.onCompleted();
          } else {
              traceError("getQuerySuggestions", starts, error);
              grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
          }             
        });
    }

    private GetQuerySuggestionsResponse buildQuerySuggestionsResponse(
            String query, Iterable<String> suggestionSet
    ) {
        return GetQuerySuggestionsResponse.newBuilder()
                .setQuery(query)
                .addAllSuggestions(suggestionSet)
                .build();
    }

    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano()/1000);
        }
    }
    
    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return
     *       current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }

}