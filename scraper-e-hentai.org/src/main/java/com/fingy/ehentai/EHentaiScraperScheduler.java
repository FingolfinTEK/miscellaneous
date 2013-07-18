package com.fingy.ehentai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.ehentai.scrape.IndexPageScraper;
import com.fingy.ehentai.scrape.MangaInfoScraper;
import com.fingy.ehentai.scrape.SearchPageMangaLinksScraper;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class EHentaiScraperScheduler {
    private static final String                  AD_LINK_REGEX                              = ".*aprod\\.hu/hirdetes/.*";

    private static final int                     DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 60;
    private static final int                     CATEGORY_TIMEOUT                           = 20000;
    private static final int                     AVAILABLE_PROCESSORS                       = Runtime.getRuntime().availableProcessors();

    private final Logger                         logger                                     = LoggerFactory.getLogger(getClass());

    private ExecutorService                      searchPageScrapingThreadPool;
    private ExecutorService                      mangaInfoScrapingThreadPool;
    private ExecutorCompletionService<MangaInfo> mangaInfoScrapingCompletionService;

    private ScraperLinksQueue                    linksQueue;
    private Set<String>                          queuedLinks;
    private Set<MangaInfo>                       scrapedItems;

    private String                               initialUrl;
    private File                                 mangaInfoFile;
    private File                                 visitedFile;
    private File                                 queuedFile;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        ScrapeResult result = new EHentaiScraperScheduler(args[0], args[1], args[2], args[3]).doScrape();
        System.exit(result.getQueueSize());
    }

    public EHentaiScraperScheduler(final String startingUrl, final String mangaInfoFilePath, final String visitedFilePath, final String queuedFilePath) {
        searchPageScrapingThreadPool = createThreadPool(6);
        mangaInfoScrapingThreadPool = createThreadPool(4);
        mangaInfoScrapingCompletionService = new ExecutorCompletionService<>(mangaInfoScrapingThreadPool);

        linksQueue = new ScraperLinksQueue();
        queuedLinks = new LinkedHashSet<>();
        scrapedItems = new LinkedHashSet<>();

        initialUrl = startingUrl;
        mangaInfoFile = new File(mangaInfoFilePath);
        visitedFile = new File(visitedFilePath);
        queuedFile = new File(queuedFilePath);
    }

    public ScrapeResult doScrape() {
        int queuedSize = 0;
        try {
            loadMangaInfoFromFile();
            loadVisitedLinksFromFile();
            loadQueuedLinksFromFile();

            searchPageScrapingThreadPool.submit(new IndexPageScraper(initialUrl, linksQueue));

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

    private void loadMangaInfoFromFile() {
        try {
            final List<String> lines = FileUtils.readLines(mangaInfoFile);
            for (String line : lines) {
                scrapedItems.add(MangaInfo.fromString(line));
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
        AdultItemJsoupScraper.setSessionExpired(false);

        while (stillHaveLinksToBeScraped()) {
            if (AbstractAdultItemJsoupScraper.isSessionExpired()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = linksQueue.take();

                if (isLinkForMangaPage(link)) {
                    queuedLinks.add(link);
                    submitMangaInfoScrapingTask(link);
                } else {
                    submitSearchPageScrapingTask(link);
                }
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    private boolean isLinkForMangaPage(String link) {
        return Pattern.matches(AD_LINK_REGEX, link);
    }

    private boolean stillHaveLinksToBeScraped() {
        return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
    }

    private void submitSearchPageScrapingTask(final String link) {
        try {
            searchPageScrapingThreadPool.submit(new SearchPageMangaLinksScraper(link, linksQueue));
        } catch (Exception e) {
            logger.error("Exception occured", e);
        }
    }

    private void submitMangaInfoScrapingTask(final String link) {
        try {
            mangaInfoScrapingCompletionService.submit(new MangaInfoScraper(link, linksQueue));
        } catch (Exception e) {
            logger.error("Exception occured", e);
        }
    }

    private void awaitTerminationOfTheTasks() {
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(searchPageScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES, TimeUnit.MINUTES);
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(mangaInfoScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void collectAndSaveResults() throws FileNotFoundException, IOException {
        collectResults();
        FileUtils.writeLines(mangaInfoFile, scrapedItems);
    }

    private void collectResults() {
        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<MangaInfo> future = mangaInfoScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
                MangaInfo mangaInfo = future.get();
                scrapedItems.add(mangaInfo);
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
        return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * processorMultiplier, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    }
}
