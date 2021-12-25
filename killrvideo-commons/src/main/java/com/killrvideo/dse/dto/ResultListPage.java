package com.killrvideo.dse.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
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
	
	/**
     * Constructor from a RESULT.
     * 
     * @param rs
     *      result set
     */
    public ResultListPage(Result<ENTITY> rs) {
        if (null != rs) {
            Iterator<ENTITY> iterResults = rs.iterator();
            // rs.getAvailableWithoutFetching() all to parse only current page without fecthing all
            IntStream.range(0, rs.getAvailableWithoutFetching())
                     .forEach(item -> listOfResults.add(iterResults.next()));
            nextPage = Optional.ofNullable(rs.getExecutionInfo().getPagingState())
                               .map(PagingState::toString);
        }
    }
    
	/**
	 * Constructor with mapper.
	 *
	 * @param rs
	 * 		result set
	 * @param mapper
	 * 		mapper
	 */
	public ResultListPage(ResultSet rs, Mapper<ENTITY> mapper) {
		this(mapper.map(rs));
	}

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
