package com.fingy.yellowpages.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;

public abstract class AbstractYellowPagesScraper<T> extends AbstractJsoupScraper<T> {

    private final ScraperLinksQueue linksQueue;

    public AbstractYellowPagesScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }

    @Override
    protected Document getPage(final String scrapeUrl) throws IOException {
        return HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(scrapeUrl);
    }
}
