package com.fingy.mouseprice.scrape;

import java.io.IOException;
import java.util.Map;

import org.jsoup.nodes.Document;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.context.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public abstract class AbstractMousePriceScraper<T> extends AbstractJsoupScraper<T> {

    private final ScraperLinksQueue linksQueue;

    public AbstractMousePriceScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    public AbstractMousePriceScraper(final String scrapeUrl, final Map<String, String> cookies, final ScraperLinksQueue linksQueue) {
        super(cookies, scrapeUrl);
        this.linksQueue = linksQueue;
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }

    @Override
    protected Document getPage() throws IOException {
        return JsoupParserUtil.getPageFromUrlWithTimeout(getScrapeUrl(), 30000);
    }
}
