package com.fingy.pelephone.scrape;

import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.pelephone.ContactInfo;
import com.fingy.proxylist.ProxyInfo;
import com.fingy.proxylist.ProxyType;
import com.fingy.scrape.util.HttpClientParserUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ContactInfoScraperTest {
    private static final String START_URL = "http://www.pelephone.co.il//digital/3G/Corporate/digital/support/general_info/find_number/.aspx";

    private Queue<ProxyInfo> proxies = new LinkedBlockingQueue<>();
    private String name = "משה";
    private String city = "נתניה";

    @Test
    public void testScrapeLink() throws Exception {
        String proxyFilePath = getClass().getClassLoader().getResource("proxies.txt").getPath();
        List<String> proxyLines = FileUtils.readLines(new File(proxyFilePath));
        for (String proxyLine : proxyLines) {
            String[] hostAndPort = proxyLine.trim().split(":");
            proxies.add(new ProxyInfo(hostAndPort[0], hostAndPort[1], ProxyType.HTTP_PROXY));
        }

        List<ContactInfo> contactInfos = searchContacts();

        while (true) {
            scrapeTelephoneNumbers(contactInfos);
            if (allPhoneNumbersScraped(contactInfos)) {
                break;
            }
        }

        FileUtils.writeLines(new File("details.txt"), contactInfos);
    }

    private List<ContactInfo> searchContacts() {
        try {
            ProxyInfo proxy = proxies.poll();
            System.out.println("Scraping contacts with proxy " + proxy);
            HttpClientParserUtil.resetClientWithProxy(proxy.getHost(), proxy.getPort());
            proxies.add(proxy);
            return new ContactInfoScraper(city, name).call();
        } catch (Exception e) {
            System.out.println("Retrying scrape");
            return searchContacts();
        }
    }

    private void scrapeTelephoneNumbers(List<ContactInfo> contactInfos) throws IOException, InterruptedException {
        final Queue<ContactInfo> infosToBeScraped = new LinkedBlockingQueue<>();
        for (ContactInfo contact : contactInfos) {
            if (!contact.hasValidPhoneNumber()) {
                infosToBeScraped.add(contact);
            }
        }

        ThreadPoolExecutor executor = ExecutorsUtil.createThreadPool(6);
        for (int i = 0; i < 20; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    searchContacts();

                    int phoneCount = 0;
                    while (!infosToBeScraped.isEmpty()) {
                        ContactInfo contact = infosToBeScraped.remove();
                        String net = contact.getReshet();
                        String id = contact.getOrdinalIdForAjax();
                        String queryData = String.format("{net:\"%s\", id: \"%s\",cap:\"\"}", net, id);

                        try {
                            String jsonPhoneData = HttpClientParserUtil.postDataToUrlWithCookies("http://www.pelephone.co.il/digital/ws/144.asmx/GetSearch", queryData);
                            contact.setTelephoneNumber(parseJson(jsonPhoneData));
                        } catch (Exception e) {
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }

                        if (!contact.hasValidPhoneNumber()) {
                            infosToBeScraped.add(contact);
                            System.out.println("Cap reached, breaking after " + phoneCount);
                            break;
                        }

                        phoneCount++;
                    }
                }
            });
        }
        executor.awaitTermination(30, TimeUnit.MINUTES);
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
