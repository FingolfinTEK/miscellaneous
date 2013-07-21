package com.fingy.citydata.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class StatePageScraperTest {

    private static final String STATE_PAGE_HTM_LOCATION = "test_pages/state-page.htm";

    private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private StatePageScraper usPageScraper = new StatePageScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        assertThat(linksQueue.getVisitedSize()).isZero();
        assertThat(usPageScraper.scrapePage(page)).isEqualTo(122);
        assertThat(linksQueue.getSize()).isEqualTo(122);
        assertThat(linksQueue.getVisitedSize()).isEqualTo(1);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(STATE_PAGE_HTM_LOCATION).getFile();
        return new File(filePath);
    }

}
