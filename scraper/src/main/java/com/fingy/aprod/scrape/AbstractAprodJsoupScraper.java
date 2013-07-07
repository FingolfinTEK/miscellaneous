package com.fingy.aprod.scrape;

import java.util.Map;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractAprodJsoupScraper<T> extends AbstractJsoupScraper<T> {

	protected ScraperLinksQueue linksQueue;

	public AbstractAprodJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl);
		this.linksQueue = linksQueue;
	}

}