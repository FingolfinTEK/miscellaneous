package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.scrape.security.HideMyAssProxyBasedScrapeDetectionOverrider;
import com.fingy.scrape.security.ProxyBasedScrapeDetectionOverrider;
import com.fingy.scrape.security.TorNetworkProxyBasedScrapeDetectionOverride;
import com.fingy.scrape.security.util.TorUtil;
import com.fingy.scrape.util.HttpClientParserUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ContactInfoScraperTest {
    private static final String START_URL = "http://www.pelephone.co.il//digital/3G/Corporate/digital/support/general_info/find_number/.aspx";
    private ProxyBasedScrapeDetectionOverrider detectionOverrider = new HideMyAssProxyBasedScrapeDetectionOverrider();
    private String name = "משה";
    private String city = "נתניה";

    @Test
    public void testScrapeLink() throws Exception {
        detectionOverrider.initializeContext();
        //Thread.sleep(45000);

        List<ContactInfo> contactInfos = searchContacts();

        while (true) {
            scrapeTelephoneNumbers(contactInfos);

            if (allPhoneNumbersScraped(contactInfos))
                break;
            else {
                searchContacts();
            }
        }

        detectionOverrider.destroyContext();
        FileUtils.writeLines(new File("details.txt"), contactInfos);
    }

    private List<ContactInfo> searchContacts() {
        try {
            detectionOverrider.setUpProxy();
            HttpClientParserUtil.resetClient();
            return new ContactInfoScraper(city, name).call();
        } catch (Exception e) {
            System.out.println("Retrying scrape");
            return searchContacts();
        }
    }

    private void scrapeTelephoneNumbers(List<ContactInfo> contactInfos) throws IOException, InterruptedException {
        int phoneCount = 0;
        for (ContactInfo contact : contactInfos) {
            if (!contact.hasValidPhoneNumber()) {
                String net = contact.getReshet();
                String id = contact.getOrdinalIdForAjax();
                String queryData = String.format("{net:\"%s\", id: \"%s\",cap:\"\"}", net, id);
                try {
                    String jsonPhoneData = HttpClientParserUtil.postDataToUrlWithCookies("http://www.pelephone.co.il/digital/ws/144.asmx/GetSearch", queryData);
                    contact.setTelephoneNumber(parseJson(jsonPhoneData));
                } catch (Exception e) {
                }

                Thread.sleep(5000);

                if (!contact.hasValidPhoneNumber()) {
                    System.out.println("Cap reached, breaking after " + phoneCount);
                    break;
                }

                phoneCount++;
            }
        }
    }

    private boolean allPhoneNumbersScraped(List<ContactInfo> contactInfos) {
        boolean allValid = true;
        for (ContactInfo contact : contactInfos) {
            if (!contact.hasValidPhoneNumber()) {
                allValid = false;
                break;
            }
        }
        return allValid;
    }

    private String parseJson(String jsonData) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonData);
        JsonObject object = element.getAsJsonObject();
        return object.get("d").getAsString();
    }
}
