package com.killrvideo.service.rating.repository;

import com.killrvideo.service.rating.dao.VideoRatingByUserDao;
import com.killrvideo.service.rating.dao.VideoRatingDao;
import com.killrvideo.service.rating.dao.VideoRatingMapper;
import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import com.killrvideo.service.rating.request.GetUserRatingRequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingRepositoryTest {
    private RatingRepository repository;
    private VideoRatingDao videoRatingDao;
    private VideoRatingByUserDao videoRatingByUserDao;

    @BeforeEach
    public void setUp() {
        VideoRatingMapper mapper = mock(VideoRatingMapper.class);
        videoRatingDao = mock(VideoRatingDao.class);
        videoRatingByUserDao = mock(VideoRatingByUserDao.class);
        when(mapper.getVideoRatingDao()).thenReturn(videoRatingDao);
        when(mapper.getVideoRatingByUserDao()).thenReturn(videoRatingByUserDao);

        repository = new RatingRepository(mapper);
    }

    @Test
    void testRateVideo() {
        VideoRatingByUser ratingByUser = videoRatingByUser(UUID.randomUUID(), UUID.randomUUID(), 4);
        when(videoRatingDao.increment(any(), anyLong(), anyLong())).thenReturn(
                CompletableFuture.completedFuture(null)
        );
        when(videoRatingByUserDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(ratingByUser)
        );
        repository.rateVideo(ratingByUser).whenComplete((result, error) -> {
                    assertEquals(ratingByUser, result);
                    assertNull(error);
                }
        );
        verify(videoRatingDao, times(1)).increment(any(), anyLong(), anyLong());
        verify(videoRatingByUserDao, times(1)).insert(any());
    }

    @Test
    void testFindRating() {
        VideoRating rating = new VideoRating(UUID.randomUUID());
        when(videoRatingDao.findRating(any())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(rating))
        );
        repository.findRating(rating.getVideoid()).whenComplete((result, error) -> {
            assertTrue(result.isPresent());
            assertEquals(rating, result.get());
            assertNull(error);
        });
    }

    @Test
    void testFindUserRating() {
        VideoRatingByUser ratingByUser = videoRatingByUser(UUID.randomUUID(), UUID.randomUUID(), 4);

        when(videoRatingByUserDao.findUserRating(any(), any())).thenReturn(
            CompletableFuture.completedFuture(Optional.of(ratingByUser))
        );

        GetUserRatingRequestData request = getUserRatingRequestData(ratingByUser);
        repository.findUserRating(request).whenComplete((result, error) -> {
            assertTrue(result.isPresent());
            assertEquals(ratingByUser, result.get());
            assertNull(error);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private VideoRatingByUser videoRatingByUser(UUID videoid, UUID userid, int rating) {
        return new VideoRatingByUser(videoid, userid, rating);
    }

    private GetUserRatingRequestData getUserRatingRequestData(VideoRatingByUser videoRatingByUser) {
        return new GetUserRatingRequestData(videoRatingByUser.getVideoid(), videoRatingByUser.getUserid());
    }

    private VideoRating videoRating(UUID videoid){
        return new VideoRating(videoid);
    }
}