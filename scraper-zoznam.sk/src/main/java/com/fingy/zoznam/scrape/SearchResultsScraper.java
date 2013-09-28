package com.fingy.zoznam.scrape;

import org.jsoup.nodes.Document;

import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public class SearchResultsScraper extends AbstractZoznamScraper<Integer> {

    public SearchResultsScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(final Document page) {
        getLinksQueue().addIfNotVisited(getScrapeUrl());
        int totalPages = Integer.parseInt(getTotalPagesString(page));

        for (int i = 2; i <= totalPages; i++) {
            getLinksQueue().addIfNotVisited(getScrapeUrl() + i);
        }

        return totalPages;
    }

    private String getTotalPagesString(final Document page) {
        String paginatorText = JsoupParserUtil.getTagTextFromCssQuery(page, "#paginator span");
        String pages = paginatorText.replace("1/", "");
        return pages;
    }

}
