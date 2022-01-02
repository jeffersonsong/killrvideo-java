package com.killrvideo.service.video.dto

/**
 * Latest page.
 *
 * @author DataStax Developer Advocates team.
 */
data class LatestVideosPage(
    /** List of Previews.  */
    val listOfPreview: MutableList<LatestVideo>,
    /** Flag if paging state.  */
    var cassandraPagingState: String? = null,
    /** Use to return for query.  */
    var nextPageState: String? = null
) {
    constructor(): this(
        listOfPreview = mutableListOf()
    )

    val resultSize: Int
        get() = listOfPreview.size
}
