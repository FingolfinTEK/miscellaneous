package com.fingy.yellowpages.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;
import com.fingy.yellowpages.CompanyDetails;

public class CompanyDetailsScraper extends AbstractYellowPagesScraper<CompanyDetails> {

    public CompanyDetailsScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected CompanyDetails scrapePage(final Document page) {
        final String name = parseNameFromPage(page);
        final String address = parseAddressFromPage(page);
        final String phone = parsePhoneNumberFromPage(page);
        final String website = parseWebsiteFromPage(page);
        final String email = parseEmailFromPage(page);

        final CompanyDetails companyDetails = new CompanyDetails(name, address, phone, website, email);

        if (companyDetails.isValid()) {
            getLinksQueue().markVisited(getScrapeUrl());
            return companyDetails;
        }
        setScrapeCompromised(true);
        throw new ScrapeException("Scrape detected");
    }

    private String parseNameFromPage(final Document page) {
        return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard h1.fn a.url");
    }

    private String parseAddressFromPage(final Document page) {
        return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard p.primary-location span.listing-address");
    }

    private String parsePhoneNumberFromPage(final Document page) {
        return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard p.phone");
    }

    private String parseWebsiteFromPage(final Document page) {
        Element emailTag = JsoupParserUtil.getTagFromCssQuery(page, "div#basic-info div.vcard a.primary-website");
        return emailTag == null ? "N/A" : emailTag.attr("href");
    }

    private String parseEmailFromPage(final Document page) {
        Element emailTag = JsoupParserUtil.getTagFromCssQuery(page, "div#basic-info div.vcard a.track-email-business");
        return emailTag == null ? "N/A" : emailTag.attr("href").replace("mailto:", "");
    }

}
