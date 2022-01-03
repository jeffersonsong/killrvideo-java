package com.killrvideo.service.search.dto

import java.time.Instant
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Pojo representing DTO for table 'videos'.
 *
 * @author DataStax Developer Advocates team.
 */
data class Video(
    var videoid: UUID? = null,
    var userid: @NotNull UUID? = null,
    var name: String? = null,
    var description: @Size(min = 1, message = "description must not be empty") String? = null,
    var location: @Size(min = 1, message = "location must not be empty") String? = null,
    var locationType: Int? = 0,
    var previewImageLocation: String? = null,
    var tags: MutableSet<String>? = mutableSetOf(),
    var addedDate: @NotNull Instant?
) {
    companion object {
        const val COLUMN_VIDEOID = "videoid"
        const val COLUMN_USERID = "userid"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LOCATIONTYPE = "location_type"
        const val COLUMN_PREVIEW = "preview_image_location"
        const val COLUMN_TAGS = "tags"
        const val COLUMN_ADDED_DATE = "added_date"
    }
}