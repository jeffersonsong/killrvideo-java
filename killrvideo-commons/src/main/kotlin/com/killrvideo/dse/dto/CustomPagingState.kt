package com.killrvideo.dse.dto

import lombok.Getter
import org.apache.commons.lang3.StringUtils.isNotBlank
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.LongStream

/**
 * Entity handling pagination.
 *
 * @author DataStax Developer Advocates team.
 */
data class CustomPagingState private constructor(
    /**
     * List of Buckets of dates in yyyyMMdd format.
     */
    val listOfBuckets: List<String>,
    /**
     * Current Bucket index.
     */
    val currentBucket: Int,
    /**
     * Paging.
     */
    val cassandraPagingState: String?
) {
    /**
     * Get size of bucket list.
     */
    val listOfBucketsSize: Int
        get() = listOfBuckets.size

    /**
     * Current bucket value.
     */
    val currentBucketValue: String
        get() = listOfBuckets[currentBucket]

    /**
     * Increment index.
     */
    fun incCurrentBucketIndex(): CustomPagingState =
        CustomPagingState(listOfBuckets, currentBucket + 1, "")

    fun changeCassandraPagingState(newCassandraPagingState: String?): CustomPagingState =
        CustomPagingState(listOfBuckets, currentBucket, newCassandraPagingState)

    fun serialize(): String {
        val joiner = StringJoiner("_")
        listOfBuckets.forEach { joiner.add(it) }
        return "${joiner.toString()},$currentBucket,$cassandraPagingState"
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        val sb = StringBuilder("{")
        sb.append("\"currentBucket\":").append(currentBucket).append(",")
        sb.append("\"cassandraPagingState\":").append("\"").append(cassandraPagingState).append("\",")
        sb.append("\"listOfBuckets\":[")
        var first = true
        for (bucket in listOfBuckets) {
            if (!first) {
                sb.append(",")
            }
            sb.append("\"").append(bucket).append("\"")
            first = false
        }
        return sb.append("}").toString()
    }

    companion object {
        /**
         * Constants.
         */
        private val PARSE_LATEST_PAGING_STATE = Pattern.compile("((?:[0-9]{8}_){7}[0-9]{8}),([0-9]),(.*)")
        private val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC))

        /**
         * Build the first paging state if one does not already exist and return an object containing 3 elements
         * representing the initial state (List<String>, Integer, String).
         *
         * @return CustomPagingState
        </String> */
        fun buildFirstCustomPagingState(): CustomPagingState {
            val buckets = LongStream.rangeClosed(0L, 7L).boxed()
                .map { Instant.now().atZone(ZoneId.systemDefault()).minusDays(it)}
                .map { it.format(DATEFORMATTER) }
                .collect(Collectors.toList())
            return CustomPagingState(buckets, 0, null)
        }

        /**
         * Map Paging State.
         *
         * @param customPagingStateString current paging state.
         * @return current pageing state
         */
        fun deserialize(customPagingStateString: String): CustomPagingState? {
            if (isNotBlank(customPagingStateString)) {
                val matcher = PARSE_LATEST_PAGING_STATE.matcher(customPagingStateString)
                if (matcher.matches()) {
                    val buckets = matcher.group(1).split("_")
                    val currentBucket = matcher.group(2).toInt()
                    val cassandraPagingState = matcher.group(3)
                    return CustomPagingState(
                        buckets, currentBucket, cassandraPagingState
                    )
                }
            }
            return null
        }
    }
}
