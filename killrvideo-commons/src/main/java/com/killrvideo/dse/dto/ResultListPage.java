package com.killrvideo.dse.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;

/**
 * Ease usage of the paginState.
 *
 * @author DataStax Developer Advocates team.
 */
public class ResultListPage < ENTITY > {

	/** Results map as entities. */
	private List<ENTITY> listOfResults = new ArrayList<>();
	
	/** Custom management of paging state. */
	private Optional< String > nextPage = Optional.empty();

	/**
	 * Default Constructor.
	 */
	public ResultListPage() {}

	public ResultListPage(AsyncResultSet rs, Function<Row, ENTITY> mapper) {
		if (rs != null) {
			List<ENTITY> list = StreamSupport.stream(rs.currentPage().spliterator(), false)
					.map(mapper).collect(Collectors.toList());
			listOfResults.addAll(list);
			if (rs.hasMorePages() && rs.getExecutionInfo().getPagingState() != null) {
				nextPage = Optional.ofNullable(
						Bytes.toHexString(rs.getExecutionInfo().getPagingState())
				);
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != listOfResults) {
			sb.append("Results:");
			sb.append(listOfResults.toString());
		}
		if (nextPage.isPresent()) {
			sb.append("\n + pagingState is present : ");
			sb.append(nextPage.get());
		}
		return sb.toString();
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
	 * Setter for attribute 'listOfResults'.
	 * 
	 * @param comments
	 *            new value for 'comments '
	 */
	public void setresults(List<ENTITY> comments) {
		this.listOfResults = comments;
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
	 * Setter for attribute 'pagingState'.
	 * 
	 * @param pagingState
	 *            new value for 'pagingState '
	 */
	public void setPagingState(Optional<String> pagingState) {
		this.nextPage = pagingState;
	}

}
