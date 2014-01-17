package com.fingy.proz.scrape;

import com.fingy.proz.ContactInfo;
import com.fingy.scrape.context.ScraperLinksQueue;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ContactInfoScraper extends AbstractProzScraper<ContactInfo> {

    public ContactInfoScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected ContactInfo doScrapePageInternal(final Document page) {
        final String userName = getTextById(page, "userNameView");
        final String companyName = getTextById(page, "compNameView");
        final String name = getTextById(page, "fullNameView");
        final String website = getTextById(page, "siteView");
        final String address = getTextByIds(page, "addressView", "postalView", "cityView");
        final String country = getTextById(page, "countryView");
        final String phoneNumber = getTextById(page, "phoneView");
        final String email = getTextById(page, "paypalView");

        getLinksQueue().markVisited(getScrapeUrl());

        if (!"N/A".equals(userName) || !"N/A".equals(companyName) || !"N/A".equals(name))
            return new ContactInfo(userName, companyName, name, website, address, country, phoneNumber, email);

        AbstractJsoupScraper.setScrapeCompromised(true);
        throw new ScrapeException("Scrape compromised");
    }

    private String getTextById(final Document page, final String id) {
        final Element elementById = page.getElementById(id);
        return elementById == null ? "N/A" : elementById.text();
    }

    private String getTextByIds(final Document page, final String... ids) {
        String text = "";
        for (String id : ids) {
            text += getTextById(page, id) + " ";
        }
        return text.trim();
    }
}
