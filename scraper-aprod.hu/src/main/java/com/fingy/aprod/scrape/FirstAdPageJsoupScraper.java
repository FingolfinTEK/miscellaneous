package com.fingy.aprod.scrape;

import java.util.Collections;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.aprod.criteria.Category;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class FirstAdPageJsoupScraper extends AbstractAprodHuJsoupScraper<String> {

	public FirstAdPageJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl, Collections.<String, String> emptyMap(), linksQueue);
	}

	public FirstAdPageJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl, cookies, linksQueue);
	}

	@Override
	protected String scrapePage(Document page) {
		String scrapeUrl = getScrapeUrl();
		Integer lastPageNumber = getLastPageNumber(page);

		getLinksQueue().addIfNotVisited(scrapeUrl);
		for(int i = 2; i <= lastPageNumber; i++) {
			String pageLink = scrapeUrl + (scrapeUrl.contains("?") ? "&" : "?") + "page=" + i;
			getLinksQueue().addIfNotVisited(pageLink);
		}

		return scrapeUrl;
	}

	private Integer getLastPageNumber(Document page) {
		String cssQuery = "div.pager span.item a";
		Element lastPageNumber = page.select(cssQuery).last();
		return lastPageNumber == null ? 0 : Integer.parseInt(lastPageNumber.text());
	}

	public static void main(String[] args) {
		ScraperLinksQueue linksQueue = new ScraperLinksQueue();
		new FirstAdPageJsoupScraper(Category.BOOKS_MAGAZINES.getLink(), linksQueue).call();
		System.out.println(linksQueue.getQueuedLinks());
	}

}
