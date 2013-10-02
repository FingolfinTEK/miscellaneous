package com.fingy.scrape;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractScraper<T> implements Callable<T> {

    private static AtomicBoolean scrapeCompromised = new AtomicBoolean();
    protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final String scrapeUrl;

	public AbstractScraper(String scrapeUrl) {
		this.scrapeUrl = scrapeUrl;
	}

    public static boolean isScrapeCompromised() {
        return scrapeCompromised.get();
    }

    public static void setScrapeCompromised(final boolean isExpired) {
        scrapeCompromised.set(isExpired);
    }

    protected abstract T scrapeLink();

	public String getScrapeUrl() {
		return scrapeUrl;
	}

	public T call() {
		try {
			final T scrapedData =  scrapeLink();
			logger.debug("Successfully scraped link " + scrapeUrl);
			return scrapedData;
		} catch (ScrapeException e) {
			throw e;
		}
	}

}
