package com.killrvideo.service.rating.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter @AllArgsConstructor @ToString
public class GetUserRatingRequestData {
    private UUID videoid;
    private UUID userid;
}
