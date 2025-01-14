package com.killrvideo.service.rating.grpc;

import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import com.killrvideo.service.rating.repository.RatingRepository;
import com.killrvideo.service.rating.request.GetUserRatingRequestData;
import io.grpc.stub.StreamObserver;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RatingsServiceGrpcTest {
    @InjectMocks private RatingsServiceGrpc service;
    @Mock
    private MessagingDao messagingDao;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingsServiceGrpcValidator validator;
    @Mock
    private RatingsServiceGrpcMapper mapper;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testRateVideoWithValidationFailed() {
        RateVideoRequest grpcReq = RateVideoRequest.getDefaultInstance();
        StreamObserver<RateVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_RateVideo(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.rateVideo(grpcReq, grpcResObserver));
    }

    @Test
    void testRateVideoWithInsertFailed() {
        RateVideoRequest grpcReq = rateVideoRequest(UUID.randomUUID(), UUID.randomUUID(), 4);
        StreamObserver<RateVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_RateVideo(any(), any());

        VideoRatingByUser videoRatingByUser = mock(VideoRatingByUser.class);
        when(this.mapper.parseRateVideoRequest(any())).thenReturn(videoRatingByUser);

        when(ratingRepository.rateVideo(any())).thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.rateVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testRateVideo() {
        RateVideoRequest grpcReq = rateVideoRequest(UUID.randomUUID(), UUID.randomUUID(), 4);
        StreamObserver<RateVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_RateVideo(any(), any());

        VideoRatingByUser rating = mock(VideoRatingByUser.class);
        RatingsEvents.UserRatedVideo event = RatingsEvents.UserRatedVideo.getDefaultInstance();
        when(mapper.createUserRatedVideoEvent(any())).thenReturn(event);

        VideoRatingByUser videoRatingByUser = mock(VideoRatingByUser.class);
        when(this.mapper.parseRateVideoRequest(any())).thenReturn(videoRatingByUser);

        when(ratingRepository.rateVideo(any())).thenReturn(CompletableFuture.completedFuture(rating));
        when(this.messagingDao.sendEvent(any(), any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        this.service.rateVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetRating() {
        GetRatingRequest grpcReq = GetRatingRequest.getDefaultInstance();
        StreamObserver<GetRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_GetRating(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.getRating(grpcReq, grpcResObserver));
    }

    @Test
    void testGetRatingWithQueryFailed() {
        GetRatingRequest grpcReq = getRatingRequest(UUID.randomUUID());
        StreamObserver<GetRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetRating(any(), any());
        when(this.ratingRepository.findRating(any())).thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.getRating(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetRatingWithRatingPresent() {
        GetRatingRequest grpcReq = getRatingRequest(UUID.randomUUID());
        StreamObserver<GetRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetRating(any(), any());
        VideoRating rating = mock(VideoRating.class);
        GetRatingResponse response = GetRatingResponse.getDefaultInstance();
        when(mapper.mapToRatingResponse(any())).thenReturn(response);
        when(this.ratingRepository.findRating(any())).thenReturn(CompletableFuture.completedFuture(
                Optional.of(rating)
        ));

        this.service.getRating(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetRatingWithRatingAbsent() {
        GetRatingRequest grpcReq = getRatingRequest(UUID.randomUUID());
        StreamObserver<GetRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetRating(any(), any());
        when(this.ratingRepository.findRating(any())).thenReturn(CompletableFuture.completedFuture(
                Optional.empty()
        ));

        this.service.getRating(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetUserRatingWithValidationFailed() {
        GetUserRatingRequest grpcReq = GetUserRatingRequest.getDefaultInstance();
        StreamObserver<GetUserRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_GetUserRating(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.getUserRating(grpcReq, grpcResObserver));
    }

    @Test
    void testGetUserRatingWithQueryFailed() {
        GetUserRatingRequest grpcReq = getUserRatingRequest(UUID.randomUUID(), UUID.randomUUID());
        StreamObserver<GetUserRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetUserRating(any(), any());

        GetUserRatingRequestData requestData = mock(GetUserRatingRequestData.class);
        when(this.mapper.parseGetUserRatingRequest(any())).thenReturn(requestData);

        when(ratingRepository.findUserRating(any()))
                .thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.getUserRating(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetUserRatingWithUserRatingPresent() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        GetUserRatingRequest grpcReq = getUserRatingRequest(videoid, userid);
        StreamObserver<GetUserRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetUserRating(any(), any());
        VideoRatingByUser rating = mock(VideoRatingByUser.class);
        GetUserRatingResponse response = GetUserRatingResponse.getDefaultInstance();
        when(mapper.mapToUserRatingResponse(any())).thenReturn(response);

        GetUserRatingRequestData requestData = getUserRatingRequestData(videoid, userid);
        when(this.mapper.parseGetUserRatingRequest(any())).thenReturn(requestData);

        when(ratingRepository.findUserRating(any()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(rating)));

        this.service.getUserRating(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetUserRatingWithUserRatingAbsent() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        GetUserRatingRequest grpcReq = getUserRatingRequest(videoid, userid);
        StreamObserver<GetUserRatingResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetUserRating(any(), any());

        GetUserRatingRequestData requestData = getUserRatingRequestData(videoid, userid);
        when(this.mapper.parseGetUserRatingRequest(any())).thenReturn(requestData);

        when(ratingRepository.findUserRating(any()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        this.service.getUserRating(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @SuppressWarnings("SameParameterValue")
    private RateVideoRequest rateVideoRequest(UUID videoid, UUID userid, int rating) {
        return RateVideoRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setUserId(uuidToUuid(userid))
                .setRating(rating)
                .build();
    }

    private GetRatingRequest getRatingRequest(UUID videoid) {
        return GetRatingRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .build();
    }

    private GetUserRatingRequestData getUserRatingRequestData(UUID videoid, UUID userid) {
        return new GetUserRatingRequestData(videoid, userid);
    }

    private GetUserRatingRequest getUserRatingRequest(UUID videoid, UUID userid) {
        return GetUserRatingRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setUserId(uuidToUuid(userid))
                .build();
    }
}