package com.killrvideo.dse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Optional;

/**
 * Proposition of super class for search queries. Embedded search definition in an object
 * is a good practice for future evolutions. (adding fields etc.)
 *
 * @author DataStax Developer Advocates team.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class QueryDefinition implements Serializable {
    private static final long serialVersionUID = 5286278417340641649L;

    /**
     * Constants.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Useful for pageabl aueries.
     */
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * Optional pageState.
     */
    private Optional<String> pageState = Optional.empty();
}
