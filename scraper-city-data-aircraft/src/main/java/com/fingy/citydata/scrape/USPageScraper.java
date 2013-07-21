package com.fingy.citydata.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class USPageScraper extends AbstractCityDataPageScraper<Integer> {

    public USPageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(Document page) {
        Elements links = page.select("#main_body div.style1 table tbody tr td table a");

        for (Element link : links) {
            linksQueue.addIfNotVisited(link.attr("abs:href"));
        }

        return links.size();
    }

}
