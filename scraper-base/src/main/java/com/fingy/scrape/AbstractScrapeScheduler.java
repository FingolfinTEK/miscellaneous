package com.fingy.scrape;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.context.ScrapeContext;
import com.fingy.scrape.context.ScrapeDetails;
import com.fingy.scrape.context.ScrapeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public abstract class AbstractScrapeScheduler<T extends ScrapeDetails> {

    private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 5;
    private static final int DETAILS_AWAIT_TIMEOUT = 3000;

    private static Logger logger = LoggerFactory.getLogger(AbstractScrapeScheduler.class);

    private final ExecutorService workGeneratingScrapingThreadPool = createWorkGeneratingThreadPool();
    private final ExecutorService detailsScrapingThreadPool = createDetailsScrapingThreadPool();
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

    protected ExecutorService createWorkGeneratingThreadPool() {
        return createThreadPool();
    }

    protected ExecutorService createDetailsScrapingThreadPool() {
        return createThreadPool();
    }

    protected ThreadPoolExecutor createThreadPool() {
        return ExecutorsUtil.createThreadPool(1);
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

        while (context.stillHaveLinksToBeScraped(DETAILS_AWAIT_TIMEOUT)) {
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
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(workGeneratingScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
        ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(detailsScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    private void collectResults() {
        awaitTerminationOfTheTasks();
        context.collectResultsFromCompletionService(detailsScrapingCompletionService);
    }

}