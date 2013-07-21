package com.fingy.citydata.scrape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Document;

import com.fingy.citydata.model.AircraftInfo;
import com.fingy.citydata.model.AircraftRegistrationInfo;
import com.fingy.citydata.model.RegistrantInfo;
import com.fingy.citydata.model.RegistrationInfo;
import com.fingy.citydata.parser.AircraftInfoParser;
import com.fingy.citydata.parser.RegistrantInfoParser;
import com.fingy.citydata.parser.RegistrationInfoParser;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class CityPageScraper extends AbstractCityDataPageScraper<Collection<AircraftRegistrationInfo>> {

    private AircraftInfoParser aircraftInfoParser = new AircraftInfoParser();
    private RegistrationInfoParser registrationInfoParser = new RegistrationInfoParser();
    private RegistrantInfoParser registrantInfoParser = new RegistrantInfoParser();

    public CityPageScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Collection<AircraftRegistrationInfo> scrapePage(Document page) {
        String allInfos = getCleanedPageText(page);
        Collection<AircraftRegistrationInfo> airplaneInfosFromPageContent = parseAirplaneInfosFromPageContent(allInfos);
        linksQueue.markVisited(getScrapeUrl());
        return airplaneInfosFromPageContent;
    }

    private String getCleanedPageText(Document page) {
        String html = page.select("#main_body div.air").last().html();
        return html.replace("\n", "").replace("<b>", "").replace("</b>", "").replace("<br /> ", "\n").replace("<br />", "");
    }

    private Collection<AircraftRegistrationInfo> parseAirplaneInfosFromPageContent(String allInfos) {
        List<AircraftRegistrationInfo> parsedInfos = new ArrayList<>();

        String[] tokenizer = allInfos.split("\n");
        for (int i = 0; i < tokenizer.length; i++) {
            AircraftInfo aircraftInfo = aircraftInfoParser.parse(tokenizer[i++]);
            RegistrationInfo registrationInfo = registrationInfoParser.parse(tokenizer[i++]);
            RegistrantInfo registrantInfo = registrantInfoParser.parse(tokenizer[i]);

            AircraftRegistrationInfo aircraftRegistrationInfo = new AircraftRegistrationInfo(aircraftInfo, registrationInfo, registrantInfo);
            parsedInfos.add(aircraftRegistrationInfo);

            if (shouldIncrementCounter(tokenizer, i)) {
                i++;
            }
        }

        return parsedInfos;
    }

    private boolean shouldIncrementCounter(String[] tokenizer, int i) {
        return i + 1 < tokenizer.length && isDeregistrationInfoNext(tokenizer, i);
    }

    private boolean isDeregistrationInfoNext(String[] tokenizer, int i) {
        return tokenizer[i + 1].startsWith("Deregistered");
    }

}
