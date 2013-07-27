package com.fingy.citydata.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<String> citiesToScrape = new ArrayList<>();


        Elements cityLinks = page.select("#main_body div.style1 table tbody tr td div.style1 table a");
        for (Element cityLink : cityLinks) {
            citiesToScrape.add(cityLink.attr("abs:href"));
        }

        Collections.sort(citiesToScrape);
        linksQueue.addAllIfNotVisited(citiesToScrape);
        linksQueue.markVisited(getScrapeUrl());
        return cityLinks.size();
    }

}
