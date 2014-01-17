package com.fingy.proz.scrape;

import com.fingy.scrape.context.ScraperLinksQueue;
import org.jsoup.nodes.Document;

public class SearchResultPageListScraper extends AbstractProzScraper<Void> {

    private static final String PAGE_SUFFIX_FORMAT = "?p=1&submit=1&nshow=20&start=%d";
    public static final String RESULT_COUNT_CSS_QUERY = "table form table table div.rnd_box_lr div.rnd_box_ll div.rnd_box_ur div.rnd_box b";

    public SearchResultPageListScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Void doScrapePageInternal(final Document page) {
        final int resultCount = new Integer(page.select(RESULT_COUNT_CSS_QUERY).first().text());

        for (int i = 0; i < resultCount; i += 20) {
            final String pageSuffix = String.format(PAGE_SUFFIX_FORMAT, i);
            getLinksQueue().addIfNotVisited(getScrapeUrl() + pageSuffix);
        }

        return null;
    }
}
