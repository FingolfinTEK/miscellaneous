package com.fingy.adultwholesale.scrape;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultItemJsoupScraper extends AbstractAdultItemJsoupScraper {

	private static final String CATEGORY_SEPARATOR = " > ";
	private static final String IMAGE_URL = "https://www.adultwholesaledirect.com/customer/bigimage.php?id=";

	public AdultItemJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl, linksQueue);
	}

	@Override
	protected AdultItem scrapePage(Document page) {
		final String id = scrapeIdFromPage(page);
		final String title = scrapeTitleFromPage(page);
		final String category = scrapeCategoryFromPage(page);
		final String price = scrapePriceFromPage(page);
		final String stockStatus = scrapeStockStatusFromPage(page);
		final String description = scrapeDescriptionFromPage(page);
		final String productUrl = getScrapeUrl();
		final String imageUrl = determineImageUrl(id);

		linksQueue.markVisited(getScrapeUrl());
		return new AdultItem(id, title, category, price, stockStatus, description, productUrl, imageUrl);
	}

	private String scrapeIdFromPage(Document page) {
		return page.select("div.topTop").first().text().replace("Item #:", "").trim();
	}

	private String scrapeTitleFromPage(Document page) {
		return page.select("div.topname span.prodTitle").first().text();
	}

	private String scrapeCategoryFromPage(Document page) {
		final Elements navigation = page.select("td.pagetitle a");
		final List<Element> onlyCategories = navigation.subList(1, navigation.size());

		String itemCategories = "";
		for(Element category: onlyCategories)
			itemCategories += CATEGORY_SEPARATOR + category.text();

		return itemCategories.replaceFirst(CATEGORY_SEPARATOR, "");
	}

	private String scrapePriceFromPage(Document page) {
		return page.select("html body div div.prodInfo dl.details dd").first().text().trim();
	}

	private String scrapeStockStatusFromPage(Document page) {
		return page.select("html body div div.prodInfo dl.details dd a span").first().text().trim();
	}

	private String scrapeDescriptionFromPage(Document page) {
		return page.select("html body div div.descriptionBlock span.det_des").first().text().trim();
	}

	private String determineImageUrl(String id) {
		return IMAGE_URL + id;
	}

}
