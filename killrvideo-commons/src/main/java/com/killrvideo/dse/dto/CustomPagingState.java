package com.killrvideo.dse.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity handling pagination.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter @NoArgsConstructor
public class CustomPagingState implements Serializable {
    private static final long serialVersionUID = 8160171855827276965L;
    
    /**  Constants. */
    private static final Pattern PARSE_LATEST_PAGING_STATE = Pattern.compile("((?:[0-9]{8}_){7}[0-9]{8}),([0-9]),(.*)");

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));

    /** List of Buckets. */
    private List<String> listOfBuckets = new ArrayList<>();
    
    /** Current Bucket. */
    private int currentBucket = 0;
    
    /** Paging. */
    private String cassandraPagingState;

    /**
     * Build the first paging state if one does not already exist and return an object containing 3 elements
     * representing the initial state (List<String>, Integer, String).
     *
     * @return CustomPagingState
     */
    public static CustomPagingState buildFirstCustomPagingState() {
        return new CustomPagingState()
                .currentBucket(0)
                .cassandraPagingState(null)
                .listOfBuckets(LongStream.rangeClosed(0L, 7L).boxed()
                        .map(Instant.now().atZone(ZoneId.systemDefault())::minusDays)
                        .map(x -> x.format(DATEFORMATTER))
                        .collect(Collectors.toList()));
    }

    /**
     * Map Paging State.
     *
     * @param customPagingStateString
     *      current paging state.
     * @return
     *      current pageing state
     */
    public static Optional<CustomPagingState> parse(Optional<String> customPagingStateString) {
        CustomPagingState pagingState = null;
        if (customPagingStateString.isPresent()) {
            Matcher matcher = PARSE_LATEST_PAGING_STATE.matcher(customPagingStateString.get());
            if (matcher.matches()) {
                pagingState = new CustomPagingState()
                        .cassandraPagingState( matcher.group(3))
                        .currentBucket(Integer.parseInt(matcher.group(2)))
                        .listOfBuckets(Arrays.asList(matcher.group(1).split("_")));
            }
        }
        return Optional.ofNullable(pagingState);
    }

    /**
     * Get size of bucket list.
     */
    public int getListOfBucketsSize() {
        return getListOfBuckets().size();
    }

    /**
     * Builder pattern.
     *
     * @param cassandraPagingState
     *      last state
     * @return
     *      current object reference
     */
    public CustomPagingState cassandraPagingState(String cassandraPagingState) {
        setCassandraPagingState(cassandraPagingState);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param listOfBuckets
     *      list of buckets.
     * @return
     *      current object reference
     */
    public CustomPagingState listOfBuckets(List<String> listOfBuckets) {
        setListOfBuckets(listOfBuckets);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param currentBucket
     *      current bucket.
     * @return
     *      current object reference
     */
    public CustomPagingState currentBucket(int currentBucket) {
        setCurrentBucket(currentBucket);
        this.currentBucket = currentBucket;
        return this;
    }

    /**
     * Increment index.
     */
    public void incCurrentBucketIndex() {
        currentBucket++;
    }

    /**
     * Current bucket value.
     */
    public String getCurrentBucketValue() {
         return getListOfBuckets().get(getCurrentBucket());
    }

    /** {@inheritDoc} */
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
