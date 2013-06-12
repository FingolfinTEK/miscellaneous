package com.fingy.scrape.jsoup;

import org.jsoup.nodes.Document;

import com.fingy.scrape.football.MatchesByDay;

public abstract class AbstractMatchesByDayJsoupScraper extends AbstractJsoupScraper<MatchesByDay> {

	public AbstractMatchesByDayJsoupScraper(String scrapeUrl) {
		super(scrapeUrl);
	}

	protected abstract MatchesByDay populateWorkbookFromPage(Document page);

	@Override
	protected MatchesByDay scrapePage(Document page) {
		return populateWorkbookFromPage(page);
	}

}
