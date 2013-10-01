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
			final T scrapedData =  scrapeLink(scrapeUrl);
			logger.debug("Successfully scraped link " + scrapeUrl);
			return scrapedData;
		} catch (ScrapeException e) {
			throw e;
		}
	}

}
