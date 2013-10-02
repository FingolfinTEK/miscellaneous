package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.pelephone.scrape.util.HttpPostHelper;
import com.fingy.pelephone.scrape.util.JsonHelper;
import com.fingy.scrape.AbstractScraper;
import com.fingy.scrape.exception.ScrapeException;

import java.util.Map;

public class PhoneNumberScraper extends AbstractScraper<ContactInfo> {

    private static final String SEARCH_URL = "http://www.pelephone.co.il/digital/ws/144.asmx/GetSearch";

    private ContactInfo contact;
    private Map<String, String> cookies;

    public PhoneNumberScraper(ContactInfo contact, Map<String, String> cookies) {
        super(SEARCH_URL);
        this.contact = contact;
        this.cookies = cookies;
    }

    @Override
    protected ContactInfo scrapeLink() {
        scrapePhoneNumber();
        validatePhoneNumber();
        return contact;
    }

    private void scrapePhoneNumber() {
        try {
            String net = contact.getReshet();
            String id = contact.getOrdinalIdForAjax();
            String queryData = String.format("{net:\"%s\", id: \"%s\",cap:\"\"}", net, id);
            String jsonPhoneData = HttpPostHelper.postDataToUrlWithCookies(getScrapeUrl(), queryData, cookies);
            contact.setTelephoneNumber(JsonHelper.parseJson(jsonPhoneData));
        } catch (Exception e) {
            throw new ScrapeException(e);
        }
    }

    private void validatePhoneNumber() {
        if ("cap".equals(contact.getTelephoneNumber())) {
            AbstractScraper.setScrapeCompromised(true);
            throw new ScrapeException("Scrape detected!");
        }
    }
}