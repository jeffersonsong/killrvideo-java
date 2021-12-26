package com.killrvideo.dse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * Bean representing shared attributes in videos.
 *
 * @author DataStax Developer Advocates team
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public abstract class AbstractVideo extends AbstractEntity {
    private static final long serialVersionUID = -4366554197274003003L;
    
    /** Column names. */
    public static final String COLUMN_NAME    = "name";
    public static final String COLUMN_TAGS    = "tags";
    public static final String COLUMN_PREVIEW = "preview_image_location";
    
    @Length(min = 1, message = "The video name must not be empty")
    protected String name;

    protected String previewImageLocation;
}
