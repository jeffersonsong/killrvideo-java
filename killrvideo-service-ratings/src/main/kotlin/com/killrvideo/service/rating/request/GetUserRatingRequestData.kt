package com.killrvideo.service.rating.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter @AllArgsConstructor @ToString
public class GetUserRatingRequestData {
    private final UUID videoid;
    private final UUID userid;
}
