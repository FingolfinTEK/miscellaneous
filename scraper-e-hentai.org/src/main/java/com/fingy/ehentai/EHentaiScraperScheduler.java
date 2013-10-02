package com.fingy.ehentai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.scrape.AbstractScraper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.ehentai.scrape.IndexPageScraper;
import com.fingy.ehentai.scrape.MangaInfoScraper;
import com.fingy.ehentai.scrape.SearchPageMangaLinksScraper;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class EHentaiScraperScheduler {

    private static final int CATEGORY_TIMEOUT = 20000;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService searchPageScrapingThreadPool;
    private final ExecutorService mangaInfoScrapingThreadPool;
    private final ExecutorCompletionService<MangaInfo> mangaInfoScrapingCompletionService;

    private final ScraperLinksQueue linksQueue;
    private final Set<String> queuedLinks;
    private final Set<MangaInfo> scrapedItems;

    private final String initialUrl;
    private final File mangaInfoFile;
    private final File visitedFile;
    private final File queuedFile;

    public static void main(final String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        ScrapeResult result = new EHentaiScraperScheduler(args[0], args[1], args[2], args[3]).doScrape();
        System.exit(result.getQueueSize());
    }

    public EHentaiScraperScheduler(final String startingUrl, final String mangaInfoFilePath, final String visitedFilePath,
            final String queuedFilePath) {
        searchPageScrapingThreadPool = Executors.newSingleThreadExecutor();
        mangaInfoScrapingThreadPool = searchPageScrapingThreadPool; // createThreadPool(1);
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
        AbstractScraper.setScrapeCompromised(false);

        while (stillHaveLinksToBeScraped()) {
            if (AbstractScraper.isScrapeCompromised()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = linksQueue.take();

                if (isLinkForMangaPage(link)) {
                    queuedLinks.add(link);
                    submitMangaInfoScrapingTask(link);
                    Thread.sleep(2000 + new Random().nextInt(3000));
                } else {
                    submitSearchPageScrapingTask(link);
                }

                Thread.sleep(1500);
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    private boolean isLinkForMangaPage(final String link) {
        return link.contains("g.e-hentai.org/g/");
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
        int timeout = AbstractScraper.isScrapeCompromised() ? 0 : queuedLinks.size();
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(searchPageScrapingThreadPool, timeout, TimeUnit.SECONDS);
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(mangaInfoScrapingThreadPool, timeout, TimeUnit.SECONDS);
    }

    private void collectAndSaveResults() throws FileNotFoundException, IOException {
        collectResults();
        File tempFile = new File("temp.xsls");

        new MangaInfoToExcelBuilder().openExcel(mangaInfoFile.getPath()).appendToExcel(scrapedItems).writeToFile(tempFile.getPath())
                .close();

        FileUtils.forceDelete(mangaInfoFile);
        FileUtils.copyFile(tempFile, mangaInfoFile);
        FileUtils.forceDelete(tempFile);
    }

    private void collectResults() {
        long timeout = 10;
        for (int i = 0; i < queuedLinks.size(); i++) {
            try {
                final Future<MangaInfo> future = mangaInfoScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
                MangaInfo mangaInfo = future.get();
                scrapedItems.add(mangaInfo);
                logger.trace("Scraped item " + mangaInfo);
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

    private ExecutorService createThreadPool(final int processorMultiplier) {
        return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * processorMultiplier, Integer.MAX_VALUE, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());
    }
}
