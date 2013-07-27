package com.fingy.citydata.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class USPageScraperTest {

    private static final String US_PAGE_HTM_LOCATION = "test_pages/us-page.htm";

    private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private USPageScraper usPageScraper = new USPageScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);
        page.setBaseUri("http://www.city-data.com/aircraft");

        assertThat(usPageScraper.scrapePage(page)).isEqualTo(70);
        assertThat(linksQueue.getSize()).isEqualTo(70);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(US_PAGE_HTM_LOCATION).getFile();
        return new File(filePath);
    }

}
