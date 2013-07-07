package com.fingy.scrape.jsoup;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

public abstract class AbstractJsoupScraper<T> extends AbstractScraper<T> {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

	private static AtomicBoolean sessionExpired = new AtomicBoolean();

	private Map<String, String> cookies;

	public AbstractJsoupScraper(String scrapeUrl) {
		this(Collections.<String, String> emptyMap(), scrapeUrl);
	}

	public AbstractJsoupScraper(Map<String, String> cookies, String scrapeUrl) {
		super(scrapeUrl);
		this.setCookies(cookies);
	}

	public static boolean isSessionExpired() {
		return sessionExpired.get();
	}

	public static void setSessionExpired(boolean isExpired) {
		sessionExpired.set(isExpired);
	}

	protected abstract T scrapePage(Document page);

	@Override
	protected T scrapeLink(String scrapeUrl) {
		if (isSessionExpired())
			throw new ScrapeException("Session expired");
		try {
			final Document page = getPage(scrapeUrl);
			return scrapePage(page);
		} catch (Exception e) {
			processException(e);
			throw new ScrapeException("Exception parsing link " + getScrapeUrl(), e);
		}
	}

	protected Document getPage(String scrapeUrl) throws IOException {
		return Jsoup.connect(scrapeUrl).userAgent(USER_AGENT).cookies(getCookies()).timeout(0).get();
	}

	protected void processException(Exception e) {
		logger.debug("Exception occurred", e);
	}

	protected String getTagTextFromCssQuery(Element elementToQuery, String cssQuery) {
		Elements element = elementToQuery.select(cssQuery);
		return element.isEmpty() ? "N/A" : element.first().text().trim();
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

}
