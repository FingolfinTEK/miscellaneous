package com.fingy.adultwholesale.scrape;

import java.util.Map;

import org.jsoup.nodes.Document;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractAdultItemJsoupScraper extends AbstractJsoupScraper<AdultItem> {

	protected ScraperLinksQueue linksQueue;

	public AbstractAdultItemJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected AdultItem scrapePage(Document page) {
		if (page.getElementsContainingText("sessionexpired").isEmpty())
			return doScrapePage(page);
		else {
			setSessionExpired(true);
			throw new ScrapeException("Session expred");
		}
	}

	protected abstract AdultItem doScrapePage(Document page);

}