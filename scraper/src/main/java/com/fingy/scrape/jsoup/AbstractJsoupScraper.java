package com.fingy.scrape.jsoup;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractJsoupScraper<T> extends AbstractScraper<T> {

	public static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)";

	private Map<String, String> cookies;

	public AbstractJsoupScraper(Map<String, String> cookies, String scrapeUrl) {
		super(scrapeUrl);
		this.cookies = cookies;
	}

	protected abstract T scrapePage(Document page);

	@Override
	protected T scrapeLink(String scrapeUrl) {
		try {
			final Document page = getPage(scrapeUrl);
			return scrapePage(page);
		} catch (Exception e) {
			logger.error("Exception occured", e);
			processException(e);
			throw new ScrapeException("Exception parsing link " + getScrapeUrl(), e);
		}
	}

	private Document getPage(String scrapeUrl) throws IOException {
		return Jsoup.connect(scrapeUrl).userAgent(USER_AGENT).cookies(cookies).timeout(0).get();
	}

	protected void processException(Exception e) {
	}

}
