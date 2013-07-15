package com.fingy.aprod.scrape.exception;

import com.fingy.scrape.exception.ScrapeException;

public class SessionExpiredException extends ScrapeException {

	private static final long serialVersionUID = 1L;

	private final String scrapeUrl;

	public SessionExpiredException(String scrapeUrl) {
		this.scrapeUrl = scrapeUrl;
	}

	@Override
	public String getMessage() {
		return "Session expired for link " + scrapeUrl;
	}
}
