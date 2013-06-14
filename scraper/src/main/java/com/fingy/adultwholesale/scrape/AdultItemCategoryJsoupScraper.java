package com.fingy.adultwholesale.scrape;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultItemCategoryJsoupScraper extends AbstractJsoupScraper<Object> {

	private ScraperLinksQueue linksQueue;

	public AdultItemCategoryJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected Object scrapePage(Document page) {
		final List<Element> links = page.getElementsByTag("a");

		for (Element link : links) {
			final String href = removeZenIdFromLink(link.attr("href"));
			if (shouldAcceptLink(href))
				linksQueue.addAndVisitIfNotVisited(href);
		}

		return "Done";
	}

	private String removeZenIdFromLink(String href) {
		return href.replaceAll("&zenid=[0-9a-zA-Z]+", "");
	}

	private boolean shouldAcceptLink(String href) {
		return isCategoryLink(href) || isItemDescriptionPage(href);
	}

	private boolean isItemDescriptionPage(String href) {
		return href.contains("products_id");
	}

	private boolean isCategoryLink(String href) {
		return href.contains("cPath");
	}

}
