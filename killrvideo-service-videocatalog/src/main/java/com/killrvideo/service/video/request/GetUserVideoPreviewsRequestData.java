package com.killrvideo.service.video.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
public class GetUserVideoPreviewsRequestData {
    private final UUID userId;
    private final Optional<UUID> startingVideoId;
    private final Optional<Instant> startingAddedDate;
    private final Optional<Integer> pagingSize;
    private final Optional<String> pagingState;

    public GetUserVideoPreviewsRequestData(UUID userId) {
        this(
                userId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }
}
