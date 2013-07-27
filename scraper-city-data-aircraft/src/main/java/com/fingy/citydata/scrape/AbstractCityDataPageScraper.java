package com.fingy.citydata.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public abstract class AbstractCityDataPageScraper<T> extends AbstractJsoupScraper<T> {

    private static final int DEFAULT_TIMEOUT = 30000;

    protected final ScraperLinksQueue linksQueue;

    public AbstractCityDataPageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    @Override
    protected Document getPage(String scrapeUrl) throws IOException {
        return JsoupParserUtil.getPageFromUrlWithTimeout(scrapeUrl, DEFAULT_TIMEOUT);
    }

}
