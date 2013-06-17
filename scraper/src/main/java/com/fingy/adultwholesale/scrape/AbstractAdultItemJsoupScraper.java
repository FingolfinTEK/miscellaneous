package com.fingy.adultwholesale.scrape;

import java.util.Map;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractAdultItemJsoupScraper extends AbstractJsoupScraper<AdultItem> {

	protected ScraperLinksQueue linksQueue;

	public AbstractAdultItemJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected void processException(Exception e) {
		// linksQueue.addIfNotVisited(getScrapeUrl());
	}

}