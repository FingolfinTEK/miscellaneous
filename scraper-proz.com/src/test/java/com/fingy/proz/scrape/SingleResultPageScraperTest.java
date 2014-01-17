package com.fingy.proz.scrape;

import java.io.File;

import com.fingy.scrape.context.ScraperLinksQueue;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SingleResultPageScraperTest {

    private static final String PAGE_LOCATION = "translation-agencies.html";

    private ScraperLinksQueue queue = new ScraperLinksQueue();
    private SingleResultPageScraper scraper = new SingleResultPageScraper("", queue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        scraper.scrapePage(page);

        assertThat(queue.getQueuedLinks()).hasSize(20);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }
}
