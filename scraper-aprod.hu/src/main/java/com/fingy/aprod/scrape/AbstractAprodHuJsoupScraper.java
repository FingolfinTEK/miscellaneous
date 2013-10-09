package com.fingy.aprod.scrape;

import com.fingy.scrape.AbstractWorkQueueAwareScraper;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.context.ScraperLinksQueue;
import com.fingy.scrape.util.HtmlUnitParserUtil;
import com.gargoylesoftware.htmlunit.WebClient;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractAprodHuJsoupScraper<T> extends AbstractWorkQueueAwareScraper<T> {

    private ThreadLocal<WebClient> webClientHolder = new WebClientThreadLocal();

    public WebClient getWebClient() {
        return webClientHolder.get();
    }

    public AbstractAprodHuJsoupScraper(String scrapeUrl, Map<String, String> cookies, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, cookies, linksQueue);
    }

    @Override
    protected Document getPage() throws IOException {
        try {
            return HtmlUnitParserUtil.getHtmlPageFromUrlWithoutJavaScriptSupportUsingClient(webClientHolder.get(), getScrapeUrl());
        } catch (IOException e) {
            AbstractJsoupScraper.setScrapeCompromised(true);
            throw e;
        }
    }

    private static class WebClientThreadLocal extends ThreadLocal<WebClient> {
        @Override
        protected WebClient initialValue() {
            return HtmlUnitParserUtil.getWebClientWithProxyEnabledIfNeeded();
        }
    }
}