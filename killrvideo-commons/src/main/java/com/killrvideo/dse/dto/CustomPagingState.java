package com.killrvideo.dse.dto;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Entity handling pagination.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter
public class CustomPagingState implements Serializable {
    private static final long serialVersionUID = 8160171855827276965L;

    /**
     * Constants.
     */
    private static final Pattern PARSE_LATEST_PAGING_STATE = Pattern.compile("((?:[0-9]{8}_){7}[0-9]{8}),([0-9]),(.*)");

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));

    /**
     * List of Buckets of dates in yyyyMMdd format.
     */
    private final List<String> listOfBuckets;

    /**
     * Current Bucket index.
     */
    private final int currentBucket;

    /**
     * Paging.
     */
    private final String cassandraPagingState;

    private CustomPagingState(List<String> listOfBuckets, int currentBucket, String cassandraPagingState) {
        this.listOfBuckets = listOfBuckets != null ? Collections.unmodifiableList(listOfBuckets) : emptyList();
        this.currentBucket = currentBucket;
        this.cassandraPagingState = cassandraPagingState != null ? cassandraPagingState : "";
    }

    /**
     * Build the first paging state if one does not already exist and return an object containing 3 elements
     * representing the initial state (List<String>, Integer, String).
     *
     * @return CustomPagingState
     */
    public static CustomPagingState buildFirstCustomPagingState() {
        List<String> buckets = LongStream.rangeClosed(0L, 7L).boxed()
                .map(Instant.now().atZone(ZoneId.systemDefault())::minusDays)
                .map(x -> x.format(DATEFORMATTER))
                .collect(Collectors.toList());

        return new CustomPagingState(buckets, 0, null);
    }

    /**
     * Map Paging State.
     *
     * @param customPagingStateString current paging state.
     * @return current pageing state
     */
    public static Optional<CustomPagingState> deserialize(String customPagingStateString) {
        if (isNotBlank(customPagingStateString)) {
            Matcher matcher = PARSE_LATEST_PAGING_STATE.matcher(customPagingStateString);
            if (matcher.matches()) {
                List<String> buckets = Arrays.asList(matcher.group(1).split("_"));
                int currentBucket = Integer.parseInt(matcher.group(2));
                String cassandraPagingState = matcher.group(3);

                CustomPagingState customPagingState = new CustomPagingState(
                        buckets, currentBucket, cassandraPagingState
                );
                return Optional.of(customPagingState);
            }
        }
        return Optional.empty();
    }

    /**
     * Get size of bucket list.
     */
    public int getListOfBucketsSize() {
        return getListOfBuckets().size();
    }

    /**
     * Increment index.
     */
    public CustomPagingState incCurrentBucketIndex() {
        return new CustomPagingState(listOfBuckets, currentBucket + 1, "");
    }

    public CustomPagingState changeCassandraPagingState(String newCassandraPagingState) {
        return new CustomPagingState(listOfBuckets, currentBucket, newCassandraPagingState);
    }

    /**
     * Current bucket value.
     */
    public String getCurrentBucketValue() {
        return getListOfBuckets().get(getCurrentBucket());
    }

    public String serialize() {
        StringJoiner joiner = new StringJoiner("_");
        getListOfBuckets().forEach(joiner::add);
        return joiner + "," + getCurrentBucket() + "," + getCassandraPagingState();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"currentBucket\":").append(currentBucket).append(",");
        sb.append("\"cassandraPagingState\":").append("\"").append(cassandraPagingState).append("\",");
        sb.append("\"listOfBuckets\":[");
        boolean first = true;
        for (String bucket : listOfBuckets) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(bucket).append("\"");
            first = false;
        }
        return sb.append("}").toString();
    }
}
