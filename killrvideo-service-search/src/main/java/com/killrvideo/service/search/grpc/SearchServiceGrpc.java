package com.killrvideo.service.search.grpc;

import com.killrvideo.service.search.repository.SearchRepository;
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData;
import com.killrvideo.service.search.request.SearchVideosRequestData;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceGrpc.SearchServiceImplBase;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.killrvideo.utils.GrpcUtils.returnSingleResult;

/**
 * Service SEARCG.
 *
 * @author DataStax advocates Team
 */
@Service
public class SearchServiceGrpc extends SearchServiceImplBase {

    /**
     * Loger for that class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceGrpc.class);

    @Value("${killrvideo.discovery.services.search : SearchService}")
    private String serviceKey;

    private final SearchRepository searchRepository;
    private final SearchServiceGrpcValidator validator;
    private final SearchServiceGrpcMapper mapper;

    public SearchServiceGrpc(SearchRepository searchRepository, SearchServiceGrpcValidator validator, SearchServiceGrpcMapper mapper) {
        this.searchRepository = searchRepository;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchVideos(SearchVideosRequest grpcReq, StreamObserver<SearchVideosResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_SearchVideos(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        SearchVideosRequestData requestData = mapper.parseSearchVideosRequestData(grpcReq);

        // Map Result back to GRPC
        searchRepository.searchVideosAsync(requestData)
                .whenComplete((resultPage, error) -> {
                    if (error != null) {
                        traceError("searchVideos", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

                    } else {
                        traceSuccess("searchVideos", starts);
                        returnSingleResult(mapper.buildSearchGrpcResponse(resultPage, grpcReq.getQuery()), grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest grpcReq, StreamObserver<GetQuerySuggestionsResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_GetQuerySuggestions(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        GetQuerySuggestionsRequestData requestData = mapper.parseGetQuerySuggestionsRequestData(grpcReq);

        // Invoke Dao (Async)
        searchRepository.getQuerySuggestionsAsync(requestData)
                .whenComplete((suggestionSet, error) -> {
                    // Mapping back to GRPC beans
                    if (error != null) {
                        traceError("getQuerySuggestions", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

                    } else {
                        traceSuccess("getQuerySuggestions", starts);
                        GetQuerySuggestionsResponse response = mapper.buildQuerySuggestionsResponse(suggestionSet, requestData.getQuery());
                        returnSingleResult(response, grpcResObserver);
                    }
                });
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano() / 1000);
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }

}