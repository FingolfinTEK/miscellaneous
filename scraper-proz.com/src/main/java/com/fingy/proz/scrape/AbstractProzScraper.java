package com.fingy.proz.scrape;

import java.io.IOException;

import com.fingy.scrape.AbstractWorkQueueAwareScraper;
import com.fingy.scrape.context.ScraperLinksQueue;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.util.HtmlUnitParserUtil;
import org.jsoup.nodes.Document;

public abstract class AbstractProzScraper<T> extends AbstractWorkQueueAwareScraper<T> {

    public AbstractProzScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected T scrapePage(final Document page) {
        if (page.text().contains("Your IP address has been temporarily blocked")){
            AbstractJsoupScraper.setScrapeCompromised(true);
            throw new ScrapeException("Scrape compromised");
        }

        return doScrapePageInternal(page);
    }

    protected abstract T doScrapePageInternal(final Document page);
}
