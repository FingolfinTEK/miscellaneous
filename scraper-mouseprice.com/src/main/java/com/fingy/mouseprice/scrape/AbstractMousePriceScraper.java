package com.fingy.mouseprice.scrape;

import java.util.Map;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

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

    // @Override
    // protected Document getPage(String scrapeUrl) throws IOException {
    // return HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(scrapeUrl);
    // }
}
