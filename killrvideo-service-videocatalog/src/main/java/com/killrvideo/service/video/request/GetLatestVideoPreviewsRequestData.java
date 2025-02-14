package com.killrvideo.service.video.request;

import com.killrvideo.dse.dto.CustomPagingState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter @AllArgsConstructor
public class GetLatestVideoPreviewsRequestData {
    private final CustomPagingState pageState;
    private final int pageSize;
    private final Optional<Instant> startDate;
    private final Optional<UUID> startVideoId;
}
