package com.fingy.scrape.jsoup;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractJsoupScraper<T> extends AbstractScraper<T> {

	private static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)";

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
			processException(e);
			throw new ScrapeException("Exception parsing link " + getScrapeUrl(), e);
		}
	}

	private Document getPage(String scrapeUrl) throws IOException {
		return Jsoup.connect(scrapeUrl).userAgent(USER_AGENT).timeout(0).get();
	}

	protected void processException(IOException e) {
	}

}
