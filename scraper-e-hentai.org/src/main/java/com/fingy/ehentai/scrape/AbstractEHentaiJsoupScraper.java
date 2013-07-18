package com.fingy.ehentai.scrape;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractEHentaiJsoupScraper<T> extends AbstractJsoupScraper<T> {

	protected final ScraperLinksQueue linksQueue;

	public AbstractEHentaiJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl);
		this.linksQueue = linksQueue;
	}

	public ScraperLinksQueue getLinksQueue() {
		return linksQueue;
	}
}
