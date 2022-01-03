package com.killrvideo.dse.dto

/**
 * Ease usage of the pagingState.
 *
 * @author DataStax Developer Advocates team.
 */
data class ResultListPage<ENTITY>(
    /**
     * Getter for attribute 'listOfResults'.
     *
     * @return current value of 'comments'
     */
    /**
     * Results map as entities.
     */
    val results: List<ENTITY>,
    /**
     * Getter for attribute 'listOfResults'.
     *
     * @return current value of 'pagingState'
     */
    /**
     * Custom management of paging state.
     */
    val pagingState: String?
) {
    companion object {
        fun <T> empty(): ResultListPage<T> =
            ResultListPage(emptyList(), null)

        fun <T> from(element: T): ResultListPage<T> =
            ResultListPage(listOf(element), null)

        fun <T> fromNullable(element: T?): ResultListPage<T> =
            if (element != null) from(element)
            else empty()
    }
}
