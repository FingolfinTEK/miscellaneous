package com.fingy.micromedex.web.dto;

import java.util.Collection;

public class SearchResults<T> {

	private final Integer count;
	private final Collection<T> results;

	public SearchResults(Collection<T> results) {
		this.results = results;
		this.count = results.size();
	}

	public Integer getCount() {
		return count;
	}

	public Collection<T> getResults() {
		return results;
	}

}
