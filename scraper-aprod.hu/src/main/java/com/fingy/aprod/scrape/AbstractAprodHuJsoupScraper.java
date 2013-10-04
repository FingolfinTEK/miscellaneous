package com.fingy.aprod.scrape;

import java.io.IOException;
import java.util.Map;

import com.fingy.scrape.util.HtmlUnitParserUtil;
import com.fingy.scrape.util.HttpClientParserUtil;
import org.jsoup.nodes.Document;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractAprodHuJsoupScraper<T> extends AbstractJsoupScraper<T> {

	protected ScraperLinksQueue linksQueue;

	public AbstractAprodHuJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected Document getPage(String scrapeUrl) throws IOException {
		try {
			return HttpClientParserUtil.getPageFromUrl(scrapeUrl);
		} catch (IOException e) {
			AbstractAprodHuJsoupScraper.setScrapeCompromised(true);
			throw e;
		}
	}

}