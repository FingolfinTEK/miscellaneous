package com.fingy.proz.scrape;

import java.io.File;

import com.fingy.proz.ContactInfo;
import com.fingy.scrape.context.ScraperLinksQueue;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ContactInfoScraperTest {

    private static final String PAGE_LOCATION = "translator-page.html";

    private ScraperLinksQueue queue = new ScraperLinksQueue();
    private ContactInfoScraper scraper = new ContactInfoScraper("", queue);

    @Test
    public void testScrapePage() throws Exception {
        String pageToScrape = FileUtils.readFileToString(getPageFile());
        Document page = Jsoup.parse(pageToScrape);

        assertThat(scraper.scrapePage(page)).isEqualTo(new ContactInfo("N/A", "Krishna Translations", "Nitin Goyal", "http://www.krishnatranslations.com/", "SCO 144, first floor, " +
                "Backside entry Sector 24 D 160023 Chandigarh", "India", "+91 172 2680275", "N/A"));
    }

    private File getPageFile() {
        String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
        return new File(filePath);
    }
}
