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
    private UUID userId;
    private Optional<UUID> startingVideoId;
    private Optional<Instant> startingAddedDate;
    private Optional<Integer> pagingSize;
    private Optional<String> pagingState;

    public GetUserVideoPreviewsRequestData(UUID userId) {
        this(
                userId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }
}
