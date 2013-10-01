package com.fingy.zoznam.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.AbstractWorkQueueAwareScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;

public abstract class AbstractZoznamScraper<T> extends AbstractWorkQueueAwareScraper<T> {

    public AbstractZoznamScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Document getPage() throws IOException {
        return HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(getScrapeUrl());
    }

}