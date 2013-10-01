package com.fingy.ehentai.scrape;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.ehentai.MangaInfo;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class MangaInfoScraper extends AbstractEHentaiJsoupScraper<MangaInfo> {

    private static final Collection<String> TAGS_TO_SCRAPE = Arrays.asList("parody", "character", "group", "artist", "male", "female");

    public MangaInfoScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
        super(scrapeUrl, linksQueue);
    }

    @Override
    protected MangaInfo scrapePage(Document page) {
        if (page.text().contains("Content Warning")) {
            String viewGalleryUrl = page.select("html body div:eq(2) a").attr("href");
            return scrapeLink();
        }

        String title = page.title();
        String url = getScrapeUrl();
        String images = scrapeImageInfoFromPage(page);
        String tags = scrapeTagsFromPage(page);
        String coverImageUrl = scrapeCoverImageUrlFromPage(page);
        linksQueue.markVisited(getScrapeUrl());
        return new MangaInfo(title, url, images, tags, coverImageUrl);
    }

    private String scrapeImageInfoFromPage(Document page) {
        return page.select("div#gdd table tbody tr td.gdt2").get(1).text();
    }

    private String scrapeTagsFromPage(Document page) {
        StringBuilder tags = new StringBuilder();

        Elements tagElements = page.getElementById("taglist").select("tr");
        for (Element element : tagElements) {
            String rowText = element.text();
            String tag = rowText.split(":")[0];

            if (TAGS_TO_SCRAPE.contains(tag)) {
                tags.append(rowText).append("\n");
            }
        }

        return tags.toString();
    }

    private String scrapeCoverImageUrlFromPage(Document page) {
        String imagePageUrl = page.select("div#gdt div.gdtm div a").first().attr("href");
        try {
            Document imagePage = getPage();
            return imagePage.getElementById("img").attr("src");
        } catch (IOException e) {
            logger.error("Error fetching image from page " + imagePageUrl, e);
        }

        return null;
    }

}
