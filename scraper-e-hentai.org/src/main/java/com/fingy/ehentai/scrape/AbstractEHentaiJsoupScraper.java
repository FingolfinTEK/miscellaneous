package com.fingy.ehentai.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;

public abstract class AbstractEHentaiJsoupScraper<T> extends AbstractJsoupScraper<T> {

    private static final String BANNED_IP_MESSAGE_START = "Your IP address has been temporarily banned";

    protected final ScraperLinksQueue linksQueue;

    public AbstractEHentaiJsoupScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    @Override
    protected Document getPage() throws IOException {
        try {
            Document page = HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(getScrapeUrl());

            if (!page.text().startsWith(BANNED_IP_MESSAGE_START)) {
                return page;
            }
        } catch (Exception e) {
            logger.error("Exception while getting page", e);
        }

        setScrapeCompromised(true);
        throw new ScrapeException("Session expired");
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }
}
