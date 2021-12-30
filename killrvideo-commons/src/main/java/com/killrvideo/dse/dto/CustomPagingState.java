package com.killrvideo.dse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Entity handling pagination.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter
@Setter
@AllArgsConstructor
public class CustomPagingState implements Serializable {
    private static final long serialVersionUID = 8160171855827276965L;

    /**
     * Constants.
     */
    private static final Pattern PARSE_LATEST_PAGING_STATE = Pattern.compile("((?:[0-9]{8}_){7}[0-9]{8}),([0-9]),(.*)");

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));

    /**
     * List of Buckets.
     */
    private final List<String> listOfBuckets;

    /**
     * Current Bucket.
     */
    private int currentBucket = 0;

    /**
     * Paging.
     */
    private final String cassandraPagingState;

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
    public static Optional<CustomPagingState> parse(String customPagingStateString) {
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
     * Create a paging state string from the passed in parameters
     *
     * @param buckets
     * @param bucketIndex
     * @param rowsPagingState
     * @return String
     */
    public static String createPagingState(List<String> buckets, int bucketIndex, String rowsPagingState) {
        StringJoiner joiner = new StringJoiner("_");
        buckets.forEach(joiner::add);
        return joiner + "," + bucketIndex + "," + rowsPagingState;
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
    public void incCurrentBucketIndex() {
        currentBucket++;
    }

    /**
     * Current bucket value.
     */
    public String getCurrentBucketValue() {
        return getListOfBuckets().get(getCurrentBucket());
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
