package com.fingy.ehentai.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.context.ScraperLinksQueue;

public class IndexPageScraper extends AbstractEHentaiJsoupScraper<Integer>{


	public IndexPageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl, linksQueue);
	}

	@Override
	protected Integer scrapePage(Document page) {
		Elements pages = page.select("table.ptt td a");
		Integer lastPage = Integer.parseInt(getLastButOneElementFrom(pages).text());

		enqueuePagesToScrape(lastPage);
		return lastPage;
	}

	private void enqueuePagesToScrape(Integer lastPage) {
	    linksQueue.add(getScrapeUrl());
		for (int i = 1; i < lastPage; i++) {
			String pageUrl = getScrapeUrl() + "&page=" + i;
			linksQueue.add(pageUrl);
		}
	}

	private Element getLastButOneElementFrom(Elements pages) {
		return pages.get(pages.size() - 2);
	}

}
