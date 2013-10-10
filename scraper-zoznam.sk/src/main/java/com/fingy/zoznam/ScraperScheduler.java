package com.fingy.zoznam;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.scrape.AbstractScrapeScheduler;
import com.fingy.scrape.context.ScrapeContext;
import com.fingy.zoznam.scrape.ContactInfoScraper;
import com.fingy.zoznam.scrape.SearchResultsScraper;
import com.fingy.zoznam.scrape.SingleResultPageScraper;

public class ScraperScheduler extends AbstractScrapeScheduler<ContactInfo> {

    private final String startUrl;

    public ScraperScheduler(final ScrapeContext context, final String queryUrl) {
        super(context);
        startUrl = queryUrl;
    }

    @Override
    protected void doSpecificInitialization() {
        getWorkGeneratingScrapingThreadPool().submit(new SearchResultsScraper(startUrl, context.getLinksQueue()));
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
        return link.contains("telefonne-cislo");
    }

    @Override
    public ThreadPoolExecutor createThreadPool() {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(5000);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 6, 1, TimeUnit.HOURS, workQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }
}
