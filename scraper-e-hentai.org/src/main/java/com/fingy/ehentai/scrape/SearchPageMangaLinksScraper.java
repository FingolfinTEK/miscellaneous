package com.fingy.ehentai.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.scrape.context.ScraperLinksQueue;

public class SearchPageMangaLinksScraper extends AbstractEHentaiJsoupScraper<Integer> {

    public SearchPageMangaLinksScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer scrapePage(Document page) {
        Elements mangaLinks = page.select("table.itg td.itd div.it5 a");
        for (Element mangaLink : mangaLinks) {
            linksQueue.addIfNotVisited(mangaLink.attr("href"));
        }

        linksQueue.markVisited(getScrapeUrl());
        return mangaLinks.size();
    }

}
