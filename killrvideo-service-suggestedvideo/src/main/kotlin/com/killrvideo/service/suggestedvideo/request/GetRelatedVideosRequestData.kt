package com.killrvideo.service.suggestedvideo.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.UUID;

@Getter @AllArgsConstructor @ToString
public class GetRelatedVideosRequestData {
    private final UUID videoid;
    private final int pageSize;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> pagingState;
}
