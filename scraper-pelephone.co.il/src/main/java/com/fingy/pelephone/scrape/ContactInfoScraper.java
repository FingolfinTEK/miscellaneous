package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.pelephone.scrape.util.ContactInfoXmlParser;
import com.fingy.pelephone.scrape.util.HttpPostHelper;
import com.fingy.pelephone.scrape.util.JsonHelper;
import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

import java.util.List;
import java.util.Map;

public class ContactInfoScraper extends AbstractScraper<List<ContactInfo>> {
    private static final String SEARCH_URL = "https://www.pelephone.co.il/digital/ws/144.asmx/B144Search";
    private static final String SEARCH_DATA_FORMAT = "{name:\"%s\", city: \"%s\", street: \"\", house: \"\", " + "CaptchaImage: \"null\"}";

    private String city;
    private String name;
    private Map<String, String> cookies;

    public ContactInfoScraper(String city, String name, Map<String, String> cookies) {
        super(SEARCH_URL);
        this.city = city;
        this.name = name;
        this.cookies = cookies;
    }

    @Override
    protected List<ContactInfo> scrapeLink() {
        try {
            String jsonData = HttpPostHelper.postDataToUrlWithCookies(getScrapeUrl(), String.format(SEARCH_DATA_FORMAT, name, city), cookies);
            logger.debug("Received response from server " + jsonData);
            return parseContactsFromResponse(jsonData);
        } catch (Exception e) {
            throw new ScrapeException(e);
        }
    }


    private List<ContactInfo> parseContactsFromResponse(String jsonData) {
        String xmlResults = JsonHelper.parseJson(jsonData);
        logger.debug("Extracted XML from response " + xmlResults);
        ContactInfoXmlParser xmlParser = new ContactInfoXmlParser();
        return xmlParser.parse(xmlResults);
    }

}
