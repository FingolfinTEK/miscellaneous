package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.util.HttpClientParserUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;

public class ContactInfoScraper extends AbstractScraper<List<ContactInfo>> {
    private static final String SEARCH_URL = "http://www.pelephone.co.il/digital/ws/144.asmx/B144Search";
    private static final String SEARCH_DATA_FORMAT = "{name:\"%s\", city: \"%s\", street: \"\", house: \"\", " +
            "CaptchaImage: \"null\"}";

    private String city;
    private String name;

    public ContactInfoScraper(String city, String name) {
        super(SEARCH_URL);
        this.city = city;
        this.name = name;
    }

    @Override
    protected List<ContactInfo> scrapeLink() {
        try {
            String jsonData = HttpClientParserUtil.postDataToUrlWithCookies(SEARCH_URL, String.format(SEARCH_DATA_FORMAT, name, city));
            return parseContactsFromResponse(jsonData);
        } catch (Exception e) {
            throw new ScrapeException(e);
        }
    }

    private List<ContactInfo> parseContactsFromResponse(String jsonData) {
        String xmlResults = parseJson(jsonData);
        ContactInfoXmlParser xmlParser = new ContactInfoXmlParser();
        return xmlParser.parse(xmlResults);
    }

    private String parseJson(String jsonData) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonData);
        JsonObject object = element.getAsJsonObject();
        return object.get("d").getAsString();
    }
}
