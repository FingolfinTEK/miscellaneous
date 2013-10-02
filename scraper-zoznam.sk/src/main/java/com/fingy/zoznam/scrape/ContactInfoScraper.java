package com.fingy.zoznam.scrape;

import org.jsoup.nodes.Document;

import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;
import com.fingy.zoznam.ContactInfo;

public class ContactInfoScraper extends AbstractZoznamScraper<ContactInfo> {

    public ContactInfoScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected ContactInfo scrapePage(final Document page) {
        String name = JsoupParserUtil.getTagTextFromCssQuery(page, "#detail-info div.left h1");
        String address = JsoupParserUtil.getTagTextFromCssQuery(page, "#detail-info div.left p");
        String phoneNumber = JsoupParserUtil.getTagTextFromCssQuery(page, "#detail-info1 span.phone");

        if (!"N/A".equals(name)) {
            getLinksQueue().markVisited(getScrapeUrl());
            return new ContactInfo(name, address, phoneNumber);
        }

        setScrapeCompromised(true);
        throw new ScrapeException("Scrape compromised");
    }
}
