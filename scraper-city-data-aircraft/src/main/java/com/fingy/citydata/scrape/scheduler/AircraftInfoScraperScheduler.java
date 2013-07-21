package com.fingy.citydata.scrape.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.citydata.model.AircraftRegistrationInfo;
import com.fingy.citydata.scrape.CityPageScraper;
import com.fingy.citydata.scrape.StatePageScraper;
import com.fingy.citydata.scrape.USPageScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AircraftInfoScraperScheduler {

    private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 30;
    private static final int CATEGORY_TIMEOUT = 20000;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService stateScrapingThreadPool;
    private ExecutorService infoScrapingThreadPool;
    private ExecutorCompletionService<Collection<AircraftRegistrationInfo>> infoScrapingCompletionService;

    private ScraperLinksQueue linksQueue;
    private Set<String> queuedLinks;
    private Set<AircraftRegistrationInfo> scrapedItems;

    private File infoFile;
    private File visitedFile;
    private File queuedFile;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        ScrapeResult result = new AircraftInfoScraperScheduler(args[0], args[1], args[2]).doScrape();
        System.exit(result.getQueueSize());
    }

    public AircraftInfoScraperScheduler(final String scrapedFilePath, final String visitedFilePath, final String queuedFilePath) {
        stateScrapingThreadPool = createThreadPool(6);
        infoScrapingThreadPool = createThreadPool(6);
        infoScrapingCompletionService = new ExecutorCompletionService<>(infoScrapingThreadPool);

        linksQueue = new ScraperLinksQueue();
        queuedLinks = new LinkedHashSet<>();
        scrapedItems = new LinkedHashSet<>();

        infoFile = new File(scrapedFilePath);
        visitedFile = new File(visitedFilePath);
        queuedFile = new File(queuedFilePath);
    }

    public ScrapeResult doScrape() {
        int queuedSize = 0;
        try {
            loadVisitedLinksFromFile();
            loadQueuedLinksFromFile();

            stateScrapingThreadPool.submit(new USPageScraper("http://www.city-data.com/aircraft/", linksQueue));

            submitScrapingTasksWhileThereIsEnoughWork();
            awaitTerminationOfTheTasks();
            collectAndSaveResults();
        } catch (Exception e) {
            logger.error("Exception occured", e);
        } finally {
            saveVisitedLinksToFile();
            queuedSize = saveQueuedLinksToFile();
        }

        logger.trace("Scraped items: " + scrapedItems.size());
        return new ScrapeResult(queuedSize, scrapedItems.size());
    }

    private void loadVisitedLinksFromFile() {
        try {
            final Set<String> visited = new HashSet<String>(FileUtils.readLines(visitedFile));
            linksQueue.markAllVisited(visited);
            logger.trace("Found " + visited.size() + " visited links");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void loadQueuedLinksFromFile() {
        try {
            final Set<String> queued = new HashSet<String>(FileUtils.readLines(queuedFile));
            linksQueue.addAllIfNotVisited(queued);
            logger.trace("Found " + queued.size() + " queued links; queue size: " + linksQueue.getSize());
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void submitScrapingTasksWhileThereIsEnoughWork() {
        AdultItemJsoupScraper.setSessionExpired(false);

        while (stillHaveLinksToBeScraped()) {
            if (AbstractAdultItemJsoupScraper.isSessionExpired()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = linksQueue.take();

                if (isLinkForCityPage(link)) {
                    queuedLinks.add(link);
                    submitInfoScrapingTask(link);
                } else {
                    submitSearchPageScrapingTask(link);
                }
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    private boolean isLinkForCityPage(String link) {
        return link.startsWith("http://www.city-data.com/aircraft/air-");
    }

    private boolean stillHaveLinksToBeScraped() {
        return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
    }

    private void submitSearchPageScrapingTask(final String link) {
        try {
            stateScrapingThreadPool.submit(new StatePageScraper(link, linksQueue));
        } catch (Exception e) {
            logger.error("Exception occured", e);
        }
    }

    private void submitInfoScrapingTask(final String link) {
        try {
            infoScrapingCompletionService.submit(new CityPageScraper(link, linksQueue));
        } catch (Exception e) {
            logger.error("Exception occured", e);
        }
    }

    private void awaitTerminationOfTheTasks() {
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(stateScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(infoScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
    }

    private void collectAndSaveResults() throws FileNotFoundException, IOException {
        collectResults();
        FileUtils.writeLines(infoFile, scrapedItems);
    }

    private void collectResults() {
        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<Collection<AircraftRegistrationInfo>> future = infoScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
                Collection<AircraftRegistrationInfo> registrationInfos = future.get();
                scrapedItems.addAll(registrationInfos);
            } catch (Exception e) {
                timeout = 0;
            }
        }
    }

    private void saveVisitedLinksToFile() {
        try {
            FileUtils.writeLines(visitedFile, linksQueue.getVisitedLinks());
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private int saveQueuedLinksToFile() {
        try {
            Set<String> temp = new HashSet<String>();
            temp.addAll(linksQueue.getQueuedLinks());
            temp.addAll(queuedLinks);
            temp.removeAll(linksQueue.getVisitedLinks());

            FileUtils.writeLines(queuedFile, temp);
            return temp.size();
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }

        return 0;
    }

    private ExecutorService createThreadPool(int processorMultiplier) {
        return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * processorMultiplier, Integer.MAX_VALUE, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());
    }
}
