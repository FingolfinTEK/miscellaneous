package com.fingy.scrape.context;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapeContext {

    private static final String ENCODING = "UTF-8";

    private static Logger logger = LoggerFactory.getLogger(ScrapeContext.class);

    private final ScraperLinksQueue linksQueue = new ScraperLinksQueue();
    private final DetailsLoader<?> detailsLoader;

    private Set<String> visitedLinks = new HashSet<>();
    private Set<String> queuedLinks = new HashSet<>();
    private Set<Object> scrapedItems = new HashSet<>();

    private final File detailsFile;
    private final File visitedFile;
    private final File queuedFile;

    public ScrapeContext(final String detailsFilePath, final String visitedFilePath, final String queuedFilePath,
            final DetailsLoader<?> loader) {
        detailsLoader = loader;

        detailsFile = new File(detailsFilePath);
        visitedFile = new File(visitedFilePath);
        queuedFile = new File(queuedFilePath);

        queuedLinks = new LinkedHashSet<String>();
        scrapedItems = new LinkedHashSet<>();

        loadDetailsFromFile();
        loadVisitedLinksFromFile();
        loadQueuedLinksFromFile();
    }

    public ScraperLinksQueue getLinksQueue() {
        return linksQueue;
    }

    public void initialize() {
        linksQueue.markAllVisited(visitedLinks);
        linksQueue.addAllIfNotVisited(queuedLinks);
    }

    public void save() {
        saveResultsToFile();
        saveVisitedLinksToFile();
        saveQueuedLinksToFile();
    }

    public Set<String> determineQueuedLinks() {
        Set<String> temp = new HashSet<String>();
        temp.addAll(linksQueue.getQueuedLinks());
        temp.addAll(queuedLinks);
        temp.removeAll(linksQueue.getVisitedLinks());
        return temp;
    }

    private void loadDetailsFromFile() {
        try {
            final List<String> lines = FileUtils.readLines(detailsFile, ENCODING);
            for (String line : lines) {
                scrapedItems.add(detailsLoader.loadFromCSVLine(line));
            }
            logger.trace("Loaded " + lines.size() + " contacts");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void loadVisitedLinksFromFile() {
        try {
            visitedLinks = new HashSet<String>(FileUtils.readLines(visitedFile, ENCODING));
            logger.trace("Found " + visitedLinks.size() + " visited links");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void loadQueuedLinksFromFile() {
        try {
            queuedLinks = new HashSet<String>(FileUtils.readLines(queuedFile, ENCODING));
            logger.trace("Found " + queuedLinks.size() + " queued links");
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void saveResultsToFile() {
        try {
            FileUtils.writeLines(detailsFile, ENCODING, scrapedItems);
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private void saveVisitedLinksToFile() {
        try {
            FileUtils.writeLines(visitedFile, ENCODING, visitedLinks);
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }
    }

    private int saveQueuedLinksToFile() {
        try {
            FileUtils.writeLines(queuedFile, ENCODING, queuedLinks);
            return queuedLinks.size();
        } catch (IOException e) {
            logger.error("Exception occured", e);
        }

        return 0;
    }

    public String getNextLinkToScrape() throws InterruptedException {
        String link = linksQueue.take();
        queuedLinks.add(link);
        return link;
    }

    public int getScrapedItemsSize() {
        return scrapedItems.size();
    }

    public boolean stillHaveLinksToBeScraped(final long timeout) {
        return !linksQueue.delayedIsEmpty(timeout);
    }

    public <T extends ScrapeDetails> void collectResultsFromCompletionService(final ExecutorCompletionService<T> completionService) {
        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<T> future = completionService.poll(timeout, TimeUnit.SECONDS);
                final T results = future.get();
                scrapedItems.add(results);
            } catch (Exception e) {
                timeout = 0;
            }
        }

        queuedLinks = determineQueuedLinks();
        visitedLinks.addAll(linksQueue.getVisitedLinks());
    }
}