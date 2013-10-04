package com.fingy.aprod.scrape;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdPageContactJsoupScraper extends AbstractAprodHuJsoupScraper<String> {

	public AdPageContactJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(Collections.<String, String> emptyMap(), scrapeUrl, linksQueue);
	}

	public AdPageContactJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl, linksQueue);
	}

	@Override
	protected String scrapePage(Document page) {
		Collection<String> adLinksFromPage = getAdLinksFromPage(page);
		linksQueue.addAllIfNotVisited(adLinksFromPage);
		return getScrapeUrl();
	}

	private Collection<String> getAdLinksFromPage(Document page) {
		String cssQuery = "table.offers tr:not([id]) td.offer a.link";
		return extractLinksByCssQuery(page, cssQuery);
	}

	private Collection<String> extractLinksByCssQuery(Document page, String cssQuery) {
		Set<String> adLinks = new LinkedHashSet<String>();

		Elements links = page.select(cssQuery);
		for (Element element : links) {
			adLinks.add(element.attr("href"));
		}

		return adLinks;
	}

	public static void main(String[] args) {
		ScraperLinksQueue linksQueue = new ScraperLinksQueue();
		new AdPageContactJsoupScraper("http://aprod.hu/konyv-ujsag/budapest/?all_categories=all&page=564", linksQueue).call();
		System.out.println(linksQueue.getQueuedLinks());
	}

}
