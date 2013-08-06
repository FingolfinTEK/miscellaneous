package com.fingy.yellowpages.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class CustomSearchScraper extends AbstractYellowPagesScraper<Integer> {

    public static final String WWW_YELLOWPAGES_COM = "http://www.yellowpages.com";

    public CustomSearchScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(final Document page) {
        Elements companyDetailsLinks = page.select("div#results div.listing-content div.srp-business-name a.url");
        for (Element companyLink : companyDetailsLinks) {
            getLinksQueue().addIfNotVisited(companyLink.attr("href"));
        }

        addOtherPages(page);
        getLinksQueue().markVisited(getScrapeUrl());
        return companyDetailsLinks.size();
    }

    private void addOtherPages(final Document page) {
        Elements pageLinks = page.select("div#results div.pagination div.page-navigation ol.track-pagination li a");
        for (Element pageLink : pageLinks) {
            getLinksQueue().addIfNotVisited(WWW_YELLOWPAGES_COM + pageLink.attr("href"));
        }
    }
}
