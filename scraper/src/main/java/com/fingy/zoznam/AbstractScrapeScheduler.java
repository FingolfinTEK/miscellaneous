package com.fingy.zoznam;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.ScrapeResult;

public abstract class AbstractScrapeScheduler<T> {

    private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 5;
    private static final int CATEGORY_TIMEOUT = 3000;

    private static Logger logger = LoggerFactory.getLogger(AbstractScrapeScheduler.class);

    private final ExecutorService workGeneratingScrapingThreadPool = createThreadPool();
    private final ExecutorService detailsScrapingThreadPool = createThreadPool();
    private final ExecutorCompletionService<T> detailsScrapingCompletionService = new ExecutorCompletionService<>(detailsScrapingThreadPool);

    protected final ScrapeContext context;

    public AbstractScrapeScheduler(final ScrapeContext context) {
        this.context = context;
    }

    public ExecutorService getWorkGeneratingScrapingThreadPool() {
        return workGeneratingScrapingThreadPool;
    }

    public ExecutorCompletionService<T> getDetailsScrapingCompletionService() {
        return detailsScrapingCompletionService;
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
            queuedSize = context.determineQueuedLinks().size();
        }

        logger.trace("Scraped items: " + context.getScrapedItemsSize());
        return new ScrapeResult(queuedSize, context.getScrapedItemsSize());
    }

    private void initializeScraper() {
        AdultItemJsoupScraper.setScrapeCompromised(false);
        context.initialize();
        doSpecificInitialization();
    }

    protected abstract void doSpecificInitialization();

    private void submitScrapingTasksWhileThereIsEnoughWork() {

        while (context.stillHaveLinksToBeScraped(CATEGORY_TIMEOUT)) {
            if (AbstractAdultItemJsoupScraper.isScrapeCompromised()) {
                logger.trace("Session expired, breaking");
                break;
            }

            try {
                String link = context.getNextLinkToScrape();
                if (isDetailsLink(link)) {
                    submitDetailScrapingTask(link);
                } else {
                    submitWorkGeneratingTask(link);
                }
            } catch (InterruptedException e) {
                logger.error("Exception occured", e);
                break;
            }
        }
    }

    protected abstract void submitWorkGeneratingTask(final String link);

    protected abstract void submitDetailScrapingTask(final String link);

    protected abstract boolean isDetailsLink(final String link);

    private void awaitTerminationOfTheTasks() {
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(detailsScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                                                                 TimeUnit.MINUTES);
    }

    private void collectResults() {
        awaitTerminationOfTheTasks();
        ExecutorCompletionService<T> completionService = detailsScrapingCompletionService;
        context.collectResultsFromCompletionService(completionService);
    }

    public ThreadPoolExecutor createThreadPool() {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(5000);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.HOURS, workQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }

}