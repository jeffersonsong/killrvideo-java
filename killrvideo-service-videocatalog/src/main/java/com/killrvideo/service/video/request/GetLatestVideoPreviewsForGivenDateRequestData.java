package com.killrvideo.service.video.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter @AllArgsConstructor
public class GetLatestVideoPreviewsForGivenDateRequestData {
    private final String yyyymmdd;
    private final Optional<String> pagingState;
    private final int pageSize;
    private final Optional<Instant> startDate;
    private final Optional<UUID> startVideoId;
}
