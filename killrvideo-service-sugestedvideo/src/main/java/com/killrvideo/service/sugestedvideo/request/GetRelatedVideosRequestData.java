package com.killrvideo.service.sugestedvideo.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.UUID;

@Getter @AllArgsConstructor @ToString
public class GetRelatedVideosRequestData {
    private UUID videoid;
    private int pageSize;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> pagingState;
}
