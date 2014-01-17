package com.fingy.proz;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.proz.scrape.ContactInfoScraper;
import com.fingy.proz.scrape.SearchResultPageListScraper;
import com.fingy.proz.scrape.SingleResultPageScraper;
import com.fingy.scrape.AbstractScrapeScheduler;
import com.fingy.scrape.context.ScrapeContext;

public class ScraperScheduler extends AbstractScrapeScheduler<ContactInfo> {

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private final String startUrl;

    public ScraperScheduler(final ScrapeContext context, final String queryUrl) {
        super(context);
        startUrl = queryUrl;
    }

    @Override
    protected void doSpecificInitialization() {
        getWorkGeneratingScrapingThreadPool().submit(new SearchResultPageListScraper(startUrl, context.getLinksQueue()));
    }

    @Override
    protected void submitWorkGeneratingTask(final String link) {
        getWorkGeneratingScrapingThreadPool().submit(new SingleResultPageScraper(link, context.getLinksQueue()));
    }

    @Override
    protected void submitDetailScrapingTask(final String link) {
        getDetailsScrapingCompletionService().submit(new ContactInfoScraper(link, context.getLinksQueue()));
    }

    @Override
    protected boolean isDetailsLink(final String link) {
        return link.contains("profile");
    }

    @Override
    public ThreadPoolExecutor createThreadPool() {
        final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(PROCESSORS, PROCESSORS * 4, 1, TimeUnit.HOURS, workQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }

}
