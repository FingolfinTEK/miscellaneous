package com.fingy.mouseprice;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.scrape.AbstractScraper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.mouseprice.scrape.HousePricesScraper;
import com.fingy.scrape.context.ScrapeResult;
import com.fingy.scrape.context.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public class ScraperScheduler {

    private static final String ENCODING = "UTF-8";
    private static final String START_URL = "http://www.mouseprice.com/house-prices/";

    private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 5;
    private static final int CATEGORY_TIMEOUT = 3000;

    private static Logger logger = LoggerFactory.getLogger(ScraperScheduler.class);

    private final ExecutorService detailsScrapingThreadPool;
    private final ExecutorCompletionService<List<RealEstateInfo>> detailsScrapingCompletionService;

    private final ScraperLinksQueue linksQueue;
    private final List<String> zipsToScrape;

    private static Set<String> visitedLinks;
    private static Set<String> queuedLinks;
    private static Set<RealEstateInfo> scrapedItems;

    private static File detailsFile;
    private static File visitedFile;
    private static File queuedFile;

    public ScraperScheduler(final List<String> zips) {
        detailsScrapingThreadPool = createThreadPool();
        detailsScrapingCompletionService = new ExecutorCompletionService<>(detailsScrapingThreadPool);

        linksQueue = new ScraperLinksQueue();
        zipsToScrape = zips;
    }

    public ScrapeResult doScrape() {
        int queuedSize = 0;
        try {
            initializeScraper();
            submitScrapingTasksWhileThereIsEnoughWork();
            collectResults();
        } catch (Exception e) {
            logger.error("Exception occured", e);
        } finally {
            queuedSize = determineQueuedLinks().size();
        }

        logger.trace("Scraped items: " + scrapedItems.size());
        return new ScrapeResult(queuedSize, scrapedItems.size());
    }

    private void initializeScraper() {
        linksQueue.markAllVisited(visitedLinks);
        linksQueue.addAllIfNotVisited(queuedLinks);

        for (String zip : zipsToScrape) {
            linksQueue.addIfNotVisited(START_URL + zip.replace(" ", "+"));
        }
    }

    private Set<String> determineQueuedLinks() {
        Set<String> temp = new HashSet<String>();
        temp.addAll(linksQueue.getQueuedLinks());
        temp.addAll(queuedLinks);
        temp.removeAll(linksQueue.getVisitedLinks());
        return temp;
    }

    private void submitScrapingTasksWhileThereIsEnoughWork() {
        Map<String, String> cookies = getCookiesFromStartPage();
        AbstractScraper.setScrapeCompromised(false);

        while (stillHaveLinksToBeScraped()) {
            if (AbstractScraper.isScrapeCompromised()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = linksQueue.take();
                queuedLinks.add(link);
                detailsScrapingCompletionService.submit(new HousePricesScraper(link, cookies, linksQueue));
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    private Map<String, String> getCookiesFromStartPage() {
        try {
            return JsoupParserUtil.getResponseFromUrl(START_URL).cookies();
        } catch (IOException e) {
            logger.error("Error getting cookies", e);
        }

        return Collections.emptyMap();
    }

    private boolean stillHaveLinksToBeScraped() {
        return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
    }

    private void awaitTerminationOfTheTasks() {
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(detailsScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
    }

    private void collectResults() {
        awaitTerminationOfTheTasks();

        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<List<RealEstateInfo>> future = detailsScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
                final List<RealEstateInfo> results = future.get();
                scrapedItems.addAll(results);
            } catch (Exception e) {
                timeout = 0;
            }
        }

        queuedLinks = determineQueuedLinks();
        visitedLinks.addAll(linksQueue.getVisitedLinks());
    }

    public static void loadScrapeContext(final String detailsFilePath, final String visitedFilePath, final String queuedFilePath) {
        detailsFile = new File(detailsFilePath);
        visitedFile = new File(visitedFilePath);
        queuedFile = new File(queuedFilePath);

        queuedLinks = new LinkedHashSet<>();
        scrapedItems = new LinkedHashSet<>();

        loadDetailsFromFile();
        loadVisitedLinksFromFile();
        loadQueuedLinksFromFile();
    }

    public static void saveScrapeContext() {
        saveResultsToFile();
        saveVisitedLinksToFile();
        saveQueuedLinksToFile();
    }

    private static void loadDetailsFromFile() {
        try {
            final List<String> lines = FileUtils.readLines(detailsFile, ENCODING);
            for (String line : lines) {
                scrapedItems.add(RealEstateInfo.fromString(line));
            }
            logger.trace("Loaded " + lines.size() + " contacts");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private static void loadVisitedLinksFromFile() {
        try {
            visitedLinks = new HashSet<String>(FileUtils.readLines(visitedFile, ENCODING));
            logger.trace("Found " + visitedLinks.size() + " visited links");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private static void loadQueuedLinksFromFile() {
        try {
            queuedLinks = new HashSet<String>(FileUtils.readLines(queuedFile, ENCODING));
            logger.trace("Found " + queuedLinks.size() + " queued links");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private static void saveResultsToFile() {
        try {
            FileUtils.writeLines(detailsFile, ENCODING, scrapedItems);
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private static void saveVisitedLinksToFile() {
        try {
            FileUtils.writeLines(visitedFile, ENCODING, visitedLinks);
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private static int saveQueuedLinksToFile() {
        try {
            FileUtils.writeLines(queuedFile, ENCODING, queuedLinks);
            return queuedLinks.size();
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }

        return 0;
    }

    public static ThreadPoolExecutor createThreadPool() {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(5000);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.HOURS, workQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }
}
