package com.fingy.aprod;

import com.fingy.aprod.scrape.AdPageContactJsoupScraper;
import com.fingy.aprod.scrape.ContactJsoupScraper;
import com.fingy.aprod.scrape.FirstAdPageJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.AbstractScrapeScheduler;
import com.fingy.scrape.context.ScrapeContext;
import com.fingy.scrape.context.ScrapeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class AprodScraperScheduler extends AbstractScrapeScheduler<Contact> {
    private static final String AD_LINK_REGEX = ".*aprod\\.hu/hirdetes/.*";

    private String initialUrl;
    private String initialSeeksUrl;

    public static void main(String[] args) throws IOException, InterruptedException,
            ExecutionException {
        ScrapeContext scrapeContext = new ScrapeContext(args[1], args[2], args[3], null);
        ScrapeResult result = new AprodScraperScheduler(args[0], scrapeContext).doScrape();
        System.exit(result.getQueueSize());
    }

    public AprodScraperScheduler(final String startingUrl, final ScrapeContext scrapeContext) {
        super(scrapeContext);

        initialUrl = startingUrl;
        initialSeeksUrl = startingUrl + "&search[offer_seek]=seek";
    }

    @Override
    protected ExecutorService createWorkGeneratingThreadPool() {
        return ExecutorsUtil.creteThreadPool(3, 200);
    }

    @Override
    protected ExecutorService createDetailsScrapingThreadPool() {
        return ExecutorsUtil.creteThreadPool(3, 100);
    }

    @Override
    protected void doSpecificInitialization() {
        getWorkGeneratingScrapingThreadPool().submit(new FirstAdPageJsoupScraper(initialUrl, context.getLinksQueue()));
        getWorkGeneratingScrapingThreadPool().submit(new FirstAdPageJsoupScraper(initialSeeksUrl, context.getLinksQueue()));
    }

    @Override
    protected void submitWorkGeneratingTask(String link) {
        getWorkGeneratingScrapingThreadPool().submit(new AdPageContactJsoupScraper(link, context.getLinksQueue()));
    }

    @Override
    protected void submitDetailScrapingTask(String link) {
        getDetailsScrapingCompletionService().submit(new ContactJsoupScraper(link, context.getLinksQueue()));
    }

    @Override
    protected boolean isDetailsLink(String link) {
        return Pattern.matches(AD_LINK_REGEX, link);
    }
}
