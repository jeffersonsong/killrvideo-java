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
    private CustomPagingState pageState;
    private int pageSize;
    private Optional<Instant> startDate;
    private Optional<UUID> startVideoId;
}
