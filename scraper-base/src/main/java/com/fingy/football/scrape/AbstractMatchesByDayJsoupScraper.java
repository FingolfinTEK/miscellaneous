package com.fingy.football.scrape;

import java.util.HashMap;

import org.jsoup.nodes.Document;

import com.fingy.football.MatchesByDay;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;

public abstract class AbstractMatchesByDayJsoupScraper extends AbstractJsoupScraper<MatchesByDay> {

	public AbstractMatchesByDayJsoupScraper(String scrapeUrl) {
		super(new HashMap<String, String>(), scrapeUrl);
	}

	protected abstract MatchesByDay populateWorkbookFromPage(Document page);

	@Override
	protected MatchesByDay scrapePage(Document page) {
		return populateWorkbookFromPage(page);
	}

}
