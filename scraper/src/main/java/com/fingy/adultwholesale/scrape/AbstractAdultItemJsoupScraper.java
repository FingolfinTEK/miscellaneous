package com.fingy.adultwholesale.scrape;

import java.io.IOException;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractAdultItemJsoupScraper extends AbstractJsoupScraper<AdultItem> {

	protected ScraperLinksQueue linksQueue;

	public AbstractAdultItemJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected void processException(IOException e) {
		linksQueue.addIfNotVisited(getScrapeUrl());
	}

}