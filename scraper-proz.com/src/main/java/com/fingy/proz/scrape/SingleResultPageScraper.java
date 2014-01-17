package com.fingy.proz.scrape;

import com.fingy.scrape.context.ScraperLinksQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SingleResultPageScraper extends AbstractProzScraper<Integer> {

    public SingleResultPageScraper(final String scrapeUrl, final ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected Integer doScrapePageInternal(final Document page) {
        final Elements results = page.select("a:has(.icon-search)");
        for (Element result : results) {
            final String profileId = extractProfileIdFromDetailsLink(result);
            getLinksQueue().addIfNotVisited(createContactDetailsUrlFromId(profileId));
        }
        getLinksQueue().markVisited(getScrapeUrl());
        return results.size();
    }

    private String extractProfileIdFromDetailsLink(final Element result) {
        return result.attr("href").replaceAll("/profile/|\\?.+", "");
    }

    private String createContactDetailsUrlFromId(final String id) {
        final String contactUrlFormat = "http://www.proz.com/?sp=profile&sp_mode=contact&eid_s=%s";
        return String.format(contactUrlFormat, id);
    }
}
