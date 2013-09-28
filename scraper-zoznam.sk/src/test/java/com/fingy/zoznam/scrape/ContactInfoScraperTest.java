package com.fingy.zoznam.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.zoznam.ContactInfo;

public class ContactInfoScraperTest {

    private static final String PAGE_LOCATION = "contact-page.htm";

    private final ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private final ContactInfoScraper scraper = new ContactInfoScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        ContactInfo expected = new ContactInfo("AUTOBUSOVÁ DOPRAVA JAN TOUR Buček Ján", "Nesluša 617, 02341 Nesluša", "041 / 4281240");
        assertThat(scraper.scrapePage(page)).isEqualTo(expected);
        assertThat(linksQueue.getVisitedSize()).isEqualTo(1);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }

}
