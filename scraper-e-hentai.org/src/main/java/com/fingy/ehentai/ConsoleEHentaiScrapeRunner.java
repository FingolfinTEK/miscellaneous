package com.fingy.ehentai;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.proxylist.ProxyInfo;
import com.fingy.proxylist.ProxyListScraperScheduler;
import com.fingy.proxylist.ProxyType;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.security.util.TorUtil;

public class ConsoleEHentaiScrapeRunner {

    private static final int RETRY_COUNT = 60;

    private static final String SCRAPED_TXT_FILE_NAME = "scraped.xlsx";
    private static final String VISITED_TXT_FILE_NAME = "visited.txt";
    private static final String QUEUED_TXT_FILE_NAME = "queued.txt";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String startUrl;
    private Integer currentIndex;
    private List<ProxyInfo> proxies;

    private ConsoleEHentaiScrapeRunner(String startUrl) {
        this.startUrl = startUrl;
    }

    public static void main(String[] args) {
        new ConsoleEHentaiScrapeRunner(args[0]).runScrape();
    }

    public void runScrape() {
        try {
            loadProxies();
            // TorUtil.stopTor();
            // TorUtil.startAndUseTorAsProxy();

            for (int i = 0; i < RETRY_COUNT; i++) {
                scrapeWhileThereAreResults();
            }
        } catch (Exception e) {
            logger.error("Exception occured", e);
        } finally {
            // TorUtil.stopTor();
        }
    }

    private void loadProxies() throws InterruptedException, ExecutionException {
        currentIndex = 0;
        proxies = new ProxyListScraperScheduler().getProxies();
        Collections.shuffle(proxies);
    }

    private void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
        int queueSize = 1;
        while (queueSize > 0) {
            setUpProxy();
            // TorUtil.requestNewIdentity();

            ScrapeResult result = new EHentaiScraperScheduler(startUrl, SCRAPED_TXT_FILE_NAME, VISITED_TXT_FILE_NAME, QUEUED_TXT_FILE_NAME)
                    .doScrape();
            queueSize = result.getQueueSize();
        }
    }

    private void setUpProxy() {
        ProxyInfo proxy = proxies.get(currentIndex++);
        ProxyType type = proxy.getType();
        currentIndex = currentIndex % proxies.size();

        System.setProperty(type.getHostPropertyName(), proxy.getHost());
        System.setProperty(type.getPortPropertyName(), proxy.getPort());
    }

}
