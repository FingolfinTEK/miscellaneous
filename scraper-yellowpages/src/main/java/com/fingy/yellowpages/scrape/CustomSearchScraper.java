package com.fingy.yellowpages.scrape;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class CustomSearchScraper extends AbstractJsoupScraper<Integer> {


	private final ScraperLinksQueue linksQueue;

	public CustomSearchScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl);
		this.linksQueue = linksQueue;
	}

	@Override
	protected Integer scrapePage(Document page) {
		Elements companyDetailsLinks = page.select("div#results div.listing-content div.srp-business-name a.url");
		for (Element companyLink : companyDetailsLinks) {
			linksQueue.addIfNotVisited(companyLink.attr("href"));
		}

		addOtherPages(page);
		return companyDetailsLinks.size();
	}

	private void addOtherPages(Document page) {
		Element totalResultsElement = page.select("div#results div.pagination div.result-totals strong").last();
		Integer totalResults = Integer.parseInt(totalResultsElement.text());
		Integer totalPages = totalResults / 30 + Math.min(1, totalResults % 30);

		for (int i = 2; i < totalPages; i++) {
			linksQueue.addIfNotVisited(page.baseUri() + "&page=" + i);
		}
	}

	public static void main(String[] args) throws Exception {
		ScraperLinksQueue linksQueue = new ScraperLinksQueue();
		new CustomSearchScraper("http://www.yellowpages.com/search?tracks=true&search_terms=Construction+companies&geo_location_terms=CA", linksQueue).call();
		FileUtils.writeLines(new File("test.txt"), linksQueue.getQueuedLinks());
	}

}
