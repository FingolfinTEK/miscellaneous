package com.fingy.scrape.jsoup;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractJsoupScraper<T> extends AbstractScraper<T> {

	public AbstractJsoupScraper(String scrapeUrl) {
		super(scrapeUrl);
	}

	protected abstract T scrapePage(Document page);

	@Override
	protected T scrapeLink(String scrapeUrl) {
		try {
			final Document page = getPage(scrapeUrl);
			return scrapePage(page);
		} catch (IOException e) {
			throw new ScrapeException("Exception parsing link " + getScrapeUrl(), e);
		}
	}

	private Document getPage(String scrapeUrl) throws IOException {
		return Jsoup.connect(scrapeUrl).get();
	}

}
