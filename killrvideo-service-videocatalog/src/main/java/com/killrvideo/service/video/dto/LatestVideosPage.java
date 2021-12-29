package com.killrvideo.service.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

/**
 * Latest page.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LatestVideosPage {
    /** List of Previews. */
    private List<LatestVideo> listOfPreview = new ArrayList<>();
    
    /** Flag if paging state. */
    private String cassandraPagingState = "";
    
    /** Use to return for query. */
    private String nextPageState = "";

    public int getResultSize() {
        return getListOfPreview().size();
    }
}
