package com.fingy.citydata.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.citydata.model.AircraftRegistrationInfo;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class CityPageScraperTest {

    private static final String CITY_PAGE_HTM_LOCATION = "test_pages/city-page.htm";

    private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private CityPageScraper cityPageScraper = new CityPageScraper("", linksQueue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        Collection<AircraftRegistrationInfo> scraped = cityPageScraper.scrapePage(page);
        assertThat(linksQueue.getVisitedSize()).isZero();
        assertThat(scraped).hasSize(29);
        assertThat(linksQueue.getVisitedSize()).isEqualTo(1);
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(CITY_PAGE_HTM_LOCATION).getFile();
        return new File(filePath);
    }

}
