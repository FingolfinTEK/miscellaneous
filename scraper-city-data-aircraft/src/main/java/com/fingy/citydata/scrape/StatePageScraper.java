package com.fingy.citydata.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class StatePageScraper extends AbstractCityDataPageScraper<Integer> {

    public StatePageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(Document page) {
        Elements cityLinks = page.select("#main_body div.style1 table tbody tr td div.style1 table a");

        for (Element cityLink : cityLinks) {
            linksQueue.addIfNotVisited("http://www.city-data.com" + cityLink.attr("href"));
        }

        linksQueue.markVisited(getScrapeUrl());
        return cityLinks.size();
    }

}
