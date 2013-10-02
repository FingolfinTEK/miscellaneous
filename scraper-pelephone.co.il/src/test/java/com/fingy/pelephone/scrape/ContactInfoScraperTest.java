package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.scrape.util.JsoupParserUtil;
import oracle.jrockit.jfr.events.Bits;
import org.jsoup.Connection;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ContactInfoScraperTest {
    private static final String START_URL = "https://www.pelephone.co.il//digital/3G/Corporate/digital/support/general_info/find_number/.aspx";

    @Test
    public void testScrapeLink() throws Exception {
        Connection.Response response = JsoupParserUtil.getResponseFromUrl(START_URL);
        Map<String, String> cookies = response.cookies();

        String city = "נתניה";
        String name = "משה";
        ContactInfoScraper scraper = new ContactInfoScraper(city, name, cookies);

        List<ContactInfo> contactInfos = scraper.call();
        for (ContactInfo contact : contactInfos) {
            new PhoneNumberScraper(contact, cookies).scrapeLink();
            Thread.sleep(300);
        }

        System.out.println(contactInfos);
    }
}
