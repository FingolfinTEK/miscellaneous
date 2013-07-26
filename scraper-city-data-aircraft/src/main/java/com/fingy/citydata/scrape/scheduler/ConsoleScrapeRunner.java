package com.fingy.citydata.scrape.scheduler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.security.util.TorUtil;

public class ConsoleScrapeRunner {

    private static final int RETRY_COUNT = 20;

    private static final String SCRAPED_DIRECTORY_NAME = "scraped";
    private static final String VISITED_TXT_FILE_NAME = "visited.txt";
    private static final String QUEUED_TXT_FILE_NAME = "queued.txt";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        new ConsoleScrapeRunner().runScrape();
    }

    public void runScrape() {
        try {
            setUpTor();

            for (int i = 0; i < RETRY_COUNT; i++)
                scrapeWhileThereAreResults();

            stopTor();
        } catch (Exception e) {
            logger.error("Exception occured", e);
        }
    }

    private void setUpTor() {
        TorUtil.stopTor();
        TorUtil.startAndUseTorAsProxy();
        sleep(30000);
    }

    private void sleep(int millis) {
        try {
            String sleepMessage = String.format("Waiting %d seconds", millis / 1000);
            logger.debug(sleepMessage);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("Exception occured", e);
        }
    }

    private void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
        int queueSize = 1;
        while (queueSize > 0) {
            ScrapeResult result = new AircraftInfoScraperScheduler(SCRAPED_DIRECTORY_NAME, VISITED_TXT_FILE_NAME, QUEUED_TXT_FILE_NAME)
                    .doScrape();
            queueSize = result.getQueueSize();
            TorUtil.requestNewIdentity();
            sleep(10000);
        }
    }

    private void stopTor() {
        TorUtil.stopTor();
    }

}
