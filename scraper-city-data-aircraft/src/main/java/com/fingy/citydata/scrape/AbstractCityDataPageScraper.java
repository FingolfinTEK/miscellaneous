package com.fingy.citydata.scrape;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public abstract class AbstractCityDataPageScraper<T> extends AbstractJsoupScraper<T> {

    protected final ScraperLinksQueue linksQueue;

    public AbstractCityDataPageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

}
