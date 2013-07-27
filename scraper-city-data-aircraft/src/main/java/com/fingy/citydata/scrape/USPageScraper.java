package com.fingy.citydata.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<String> statesToScrape = new ArrayList<>();

        Elements links = page.select("#main_body div.style1 table tbody tr td table a");
        for (Element link : links) {
            statesToScrape.add(link.attr("abs:href"));
        }

        Collections.sort(statesToScrape);
        linksQueue.addAllIfNotVisited(statesToScrape);
        return links.size();
    }

}
