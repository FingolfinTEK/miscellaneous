package com.fingy.mouseprice.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;

public abstract class AbstractMousePriceScraper<T> extends AbstractJsoupScraper<T> {

	private final ScraperLinksQueue linksQueue;

	public AbstractMousePriceScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl);
		this.linksQueue = linksQueue;
	}

	public ScraperLinksQueue getLinksQueue() {
		return linksQueue;
	}

	@Override
	protected Document getPage(String scrapeUrl) throws IOException {
		return HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(scrapeUrl);
	}
}
