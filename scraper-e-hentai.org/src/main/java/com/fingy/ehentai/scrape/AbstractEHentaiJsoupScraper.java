package com.fingy.ehentai.scrape;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;
import com.fingy.scrape.util.JsoupParserUtil;

public abstract class AbstractEHentaiJsoupScraper<T> extends AbstractJsoupScraper<T> {

    private static final String BANNED_IP_MESSAGE_START = "Your IP address has been temporarily banned";

    protected final ScraperLinksQueue linksQueue;

    public AbstractEHentaiJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl);
        this.linksQueue = linksQueue;
    }

    @Override
    protected Document getPage(String scrapeUrl) throws IOException {
        try {
            // Document page = JsoupParserUtil.getPageFromUrl(scrapeUrl);
            Document page = HtmlUnitParserUtil.getPageFromUrl(scrapeUrl);

            if (!page.text().startsWith(BANNED_IP_MESSAGE_START)) {
                return page;
            }
        } catch (Exception e) {
            logger.error("Exception while getting page", e);
        }

        setSessionExpired(true);
        throw new ScrapeException("Session expired");
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }
}
