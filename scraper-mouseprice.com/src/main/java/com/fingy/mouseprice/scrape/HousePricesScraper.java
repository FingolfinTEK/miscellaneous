package com.fingy.mouseprice.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.mouseprice.RealEstateInfo;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public class HousePricesScraper extends AbstractMousePriceScraper<List<RealEstateInfo>> {

    private static final String START_URL = "http://www.mouseprice.com/house-prices/";

    private final String zip;

    public HousePricesScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        this(scrapeUrl, Collections.<String, String> emptyMap(), linksQueue);
    }

    public HousePricesScraper(final String scrapeUrl, final Map<String, String> cookies, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, cookies, linksQueue);
        zip = extractZipFromScrapeUrl(scrapeUrl);
    }

    private String extractZipFromScrapeUrl(final String scrapeUrl) {
        String relativeZipUrl = scrapeUrl.replace(START_URL, "");
        if (isFirstPageUrl(relativeZipUrl)) {
            return convertToHumanReadableForm(relativeZipUrl);
        }
        String relativeZipUrlWithoutPageNumber = relativeZipUrl.substring(0, relativeZipUrl.indexOf("/"));
        return convertToHumanReadableForm(relativeZipUrlWithoutPageNumber);
    }

    public String convertToHumanReadableForm(final String relativeZipUrl) {
        return relativeZipUrl.replace("+", " ").toUpperCase();
    }

    public boolean isFirstPageUrl(final String searchTerms) {
        return !searchTerms.contains("/");
    }

    @Override
    protected List<RealEstateInfo> scrapePage(final Document page) {
        if (JsoupParserUtil.getTagTextFromCssQuery(page, "div.errorSec_404 div.gm_header").equals("IP address blocked")) {
            setScrapeCompromised(true);
            throw new ScrapeException("Scrape detected");
        }

        List<RealEstateInfo> realEstateInfos = new ArrayList<>();

        Elements realEstates = page.select("div#LV_BASearch.lv_search div.lv_pricedetails");
        for (Element realEstate : realEstates) {
            String address = scrapeAddressFromSearcResultRow(realEstate);
            String type = scrapeTypeFromSearchResultRow(realEstate);
            String pricePaid = scrapePricePaidFromSearchResultRow(realEstate);
            String date = scrapeDateFromSearchResultRow(realEstate);
            String beds = scrapeBedsFromSearchResultRow(realEstate);
            String worth = scrapeWorthFromSearchResultRow(realEstate);
            realEstateInfos.add(new RealEstateInfo(zip, address, type, pricePaid, date, beds, worth));
        }

        getLinksQueue().markVisited(getScrapeUrl());
        scheduleOtherPagesForScrape(page);
        return realEstateInfos;
    }

    private String scrapeAddressFromSearcResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_address_basic");
    }

    private String scrapeTypeFromSearchResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_proptype");
    }

    private String scrapePricePaidFromSearchResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_price");
    }

    private String scrapeDateFromSearchResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_date_basic_nl");
    }

    private String scrapeBedsFromSearchResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_beds");
    }

    private String scrapeWorthFromSearchResultRow(final Element realEstate) {
        return JsoupParserUtil.getTagTextFromCssQuery(realEstate, ".lv_worth");
    }

    private void scheduleOtherPagesForScrape(final Document page) {
        Element lastPageLink = page.select("#dtPager a").last();

        if (lastPageLink != null) {
            int lastPage = Integer.parseInt(lastPageLink.text());
            for (int pageNumber = 2; pageNumber <= lastPage; pageNumber++) {
                String pageUrl = START_URL + getZipAsSearchQuery() + "/" + pageNumber;
                getLinksQueue().addIfNotVisited(pageUrl);
            }
        }
    }

    public String getZipAsSearchQuery() {
        return zip.replace(" ", "+");
    }
}
