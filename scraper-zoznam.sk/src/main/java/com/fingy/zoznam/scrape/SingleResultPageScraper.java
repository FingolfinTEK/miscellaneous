package com.fingy.zoznam.scrape;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class SingleResultPageScraper extends AbstractZoznamScraper<Integer> {

    private static final String CONTACT_LINK_SELECTOR = "#search-results li a.more-btn1";

    public SingleResultPageScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(final Document page) {
        List<String> links = scrapeContactLinks(page);
        getLinksQueue().addAllIfNotVisited(links);
        getLinksQueue().markVisited(getScrapeUrl());
        return links.size();
    }

    private List<String> scrapeContactLinks(final Document page) {
        List<String> links = new ArrayList<>();

        for (Element contactLink : page.select(CONTACT_LINK_SELECTOR)) {
            links.add(contactLink.absUrl("href"));
        }

        return links;
    }

}
