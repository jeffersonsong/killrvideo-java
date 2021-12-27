package com.killrvideo.service.video.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Latest page.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter
public class LatestVideosPage {
    /** List of Previews. */
    private List<LatestVideo> listOfPreview = new ArrayList<>();
    
    /** Flag if paging state. */
    private String cassandraPagingState = "";
    
    /** Use to return for query. */
    private String nextPageState = "";

    /**
     * Access latest videos.
     *
     * @param video
     *      adding a video
     */
    public void addLatestVideos(LatestVideo video) {
        listOfPreview.add(video);
    }

    public int getResultSize() {
        return getListOfPreview().size();
    }
}
