package com.fingy.yellowpages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;
import com.fingy.yellowpages.scrape.CompanyDetailsScraper;
import com.fingy.yellowpages.scrape.CustomSearchScraper;

public class ScraperScheduler {
    private static final String SEARCH_FORMAT = "http://www.yellowpages.com/%s/%s";

    private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 15;
    private static final int CATEGORY_TIMEOUT = 180000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService adPageScrapingThreadPool;
    private final ExecutorService contactScrapingThreadPool;
    private final ExecutorCompletionService<CompanyDetails> contactScrapingCompletionService;

    private final ScraperLinksQueue linksQueue;
    private final Set<String> queuedLinks;
    private final Set<CompanyDetails> scrapedItems;

    private final String searchTerm;
    private final String searchLocation;

    private final File contactsFile;
    private final File visitedFile;
    private final File queuedFile;

    public ScraperScheduler(final String term, final String location, final String contactsFilePath, final String visitedFilePath,
            final String queuedFilePath) {
        adPageScrapingThreadPool = Executors.newSingleThreadExecutor();
        contactScrapingThreadPool = Executors.newSingleThreadExecutor();
        contactScrapingCompletionService = new ExecutorCompletionService<>(contactScrapingThreadPool);

        linksQueue = new ScraperLinksQueue();
        queuedLinks = new LinkedHashSet<>();
        scrapedItems = new LinkedHashSet<>();

        searchTerm = term.replaceAll(" +", "+");
        searchLocation = location.replaceAll(" +", "+");

        contactsFile = new File(contactsFilePath);
        visitedFile = new File(visitedFilePath);
        queuedFile = new File(queuedFilePath);
    }

    public ScrapeResult doScrape() {
        int queuedSize = 0;
        try {
            loadDetailsFromFile();
            loadVisitedLinksFromFile();
            loadQueuedLinksFromFile();

            HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupport(CustomSearchScraper.WWW_YELLOWPAGES_COM);
            submitSearchPageScrapingTask(String.format(SEARCH_FORMAT, searchLocation, searchTerm));

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

    private void loadDetailsFromFile() {
        try {
            final List<String> lines = FileUtils.readLines(contactsFile);
            for (String line : lines) {
                scrapedItems.add(CompanyDetails.fromString(line));
            }
            logger.trace("Loaded " + lines.size() + " contacts");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
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
        AdultItemJsoupScraper.setScrapeCompromised(false);

        while (stillHaveLinksToBeScraped()) {
            if (AbstractAdultItemJsoupScraper.isScrapeCompromised()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = linksQueue.take();

                if (isLinkForSearchPage(link)) {
                    submitSearchPageScrapingTask(link);
                } else {
                    queuedLinks.add(link);
                    submitContactScrapingTask(link);
                    Thread.sleep(2000 + new Random().nextInt(4000));
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    private boolean isLinkForSearchPage(final String link) {
        return !link.contains("lid=");
    }

    private boolean stillHaveLinksToBeScraped() {
        return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
    }

    private void submitSearchPageScrapingTask(final String link) {
        adPageScrapingThreadPool.submit(new CustomSearchScraper(link, linksQueue));
    }

    private void submitContactScrapingTask(final String link) {
        contactScrapingCompletionService.submit(new CompanyDetailsScraper(link, linksQueue));
    }

    private void awaitTerminationOfTheTasks() {
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(adPageScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(contactScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
    }

    private void collectAndSaveResults() throws FileNotFoundException, IOException {
        collectResults();
        FileUtils.writeLines(contactsFile, scrapedItems);
    }

    private void collectResults() {
        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<CompanyDetails> future = contactScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
                CompanyDetails contact = future.get();

                if (contact.isValid()) {
                    scrapedItems.add(contact);
                }
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
}
