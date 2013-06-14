package com.fingy.scrape;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractScraper<T> implements Callable<T> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final String scrapeUrl;

	public AbstractScraper(String scrapeUrl) {
		this.scrapeUrl = scrapeUrl;
	}

	protected abstract T scrapeLink(String scrapeUrl);

	public String getScrapeUrl() {
		return scrapeUrl;
	}

	public T call() {
		try {
			logger.debug("Scraping link " + scrapeUrl);
			return scrapeLink(scrapeUrl);
		} catch (ScrapeException e) {
			logger.error("Exception occured", e);
			throw e;
		}
	}

}
