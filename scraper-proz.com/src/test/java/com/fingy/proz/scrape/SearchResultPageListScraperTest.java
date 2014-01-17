package com.fingy.proz.scrape;

import java.io.File;

import com.fingy.proz.scrape.SearchResultPageListScraper;
import com.fingy.scrape.context.ScraperLinksQueue;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SearchResultPageListScraperTest {

    private static final String PAGE_LOCATION = "translation-agencies.html";

    private ScraperLinksQueue queue = new ScraperLinksQueue();
    private SearchResultPageListScraper scraper = new SearchResultPageListScraper("", queue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);
        page.setBaseUri("http://www.base.com");

        scraper.scrapePage(page);

        assertThat(queue.getQueuedLinks()).hasSize(2413);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }
}
