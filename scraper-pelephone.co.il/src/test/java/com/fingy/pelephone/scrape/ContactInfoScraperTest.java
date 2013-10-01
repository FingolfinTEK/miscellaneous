package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.List;

public class ContactInfoScraperTest {
    @Test
    public void testScrapeLink() throws Exception {
        ContactInfoScraper scraper = new ContactInfoScraper("זכרון יעקב", "מנחם");
        List<ContactInfo> infos = scraper.call();
        System.out.println(infos.size());
    }
}
