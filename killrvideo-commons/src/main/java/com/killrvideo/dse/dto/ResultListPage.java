package com.killrvideo.dse.dto;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ease usage of the paginState.
 *
 * @author DataStax Developer Advocates team.
 */
@AllArgsConstructor
public class ResultListPage<ENTITY> {

    /**
     * Results map as entities.
     */
    private final List<ENTITY> listOfResults;

    /**
     * Custom management of paging state.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> nextPage;

    public static <T> ResultListPage<T> empty() {
        return new ResultListPage<>(Collections.emptyList(), Optional.empty());
    }

    public static <T> ResultListPage<T> from(T element) {
        return new ResultListPage<>(Collections.singletonList(element), Optional.empty());
    }

    /**
     * Getter for attribute 'listOfResults'.
     *
     * @return current value of 'comments'
     */
    public List<ENTITY> getResults() {
        return listOfResults;
    }

    /**
     * Getter for attribute 'listOfResults'.
     *
     * @return current value of 'pagingState'
     */
    public Optional<String> getPagingState() {
        return nextPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != listOfResults) {
            sb.append("Results:");
            sb.append(listOfResults);
        }
        if (nextPage.isPresent()) {
            sb.append("\n + pagingState is present : ");
            sb.append(nextPage.get());
        }
        return sb.toString();
    }
}
