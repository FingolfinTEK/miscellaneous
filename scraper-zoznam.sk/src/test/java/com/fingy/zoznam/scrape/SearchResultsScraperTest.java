package com.fingy.zoznam.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class SearchResultsScraperTest {

    private static final String PAGE_LOCATION = "results.htm";

    private final ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private final SearchResultsScraper scraper = new SearchResultsScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        assertThat(linksQueue.getVisitedSize()).isZero();
        assertThat(scraper.scrapePage(page)).isEqualTo(2784);
        assertThat(linksQueue.getQueuedLinks()).hasSize(2784);
        assertThat(linksQueue.getVisitedSize()).isZero();
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }

}
