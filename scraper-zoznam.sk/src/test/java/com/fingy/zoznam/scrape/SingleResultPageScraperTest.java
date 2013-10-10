package com.fingy.zoznam.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.context.ScraperLinksQueue;

public class SingleResultPageScraperTest {

    private static final String PAGE_LOCATION = "results.htm";

    private final ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private final SingleResultPageScraper scraper = new SingleResultPageScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);
        page.setBaseUri("http://telefonny.zoznam.sk");

        assertThat(linksQueue.getVisitedSize()).isZero();
        assertThat(scraper.scrapePage(page)).isEqualTo(20);
        assertThat(linksQueue.getQueuedLinks()).hasSize(20);
        assertThat(linksQueue.getVisitedSize()).isEqualTo(1);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }

}
