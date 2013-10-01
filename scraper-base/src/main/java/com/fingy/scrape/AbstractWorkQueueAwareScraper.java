package com.fingy.scrape;

import java.util.Map;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractWorkQueueAwareScraper<T> extends AbstractJsoupScraper<T> {

    private final ScraperLinksQueue linksQueue;

    public AbstractWorkQueueAwareScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    public AbstractWorkQueueAwareScraper(final String scrapeUrl, final Map<String, String> cookies, final ScraperLinksQueue linksQueue) {
        super(cookies, scrapeUrl);
        this.linksQueue = linksQueue;
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }
}
