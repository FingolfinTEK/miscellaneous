package com.fingy.adultwholesale.scrape;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultItemCategoryJsoupScraper extends AbstractAdultItemJsoupScraper {

	private static final String ITEM_URL = "https://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkproductdetails.php?prod_id=";
	private static final String CATEGORY_URL = "https://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkproducts.php?categ_id=";

	private static final Pattern ITEM_PATTERN = Pattern.compile("javascript: void\\(displayDetails\\((\\d+)\\)\\)");
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("javascript: void\\((changeCateg|toggleCateg)\\((\\d+)\\)\\)");

	public AdultItemCategoryJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl, linksQueue);
	}

	@Override
	protected AdultItem scrapePage(Document page) {
		scrapePages(page);
		scrapeCategories(page);

		linksQueue.markVisited(getScrapeUrl());
		return null;
	}

	private void scrapePages(Document page) {
		final List<Element> itemLinks = page.select("div.itemsList div.item span.title a");
		for (Element itemLink : itemLinks) {
			String link = extractItemLink(itemLink);
			linksQueue.addIfNotVisited(link);
		}
	}

	private void scrapeCategories(Document page) {
		final List<Element> categoryLinks = page.select("a.categorylinkred");
		for (Element categoryLink : categoryLinks) {
			String link = extractCategoryLink(categoryLink);
			linksQueue.addIfNotVisited(link);
		}
	}

	private String extractItemLink(Element itemLink) {
		Matcher matcher = ITEM_PATTERN.matcher(itemLink.attr("onclick"));
		if (matcher.matches())
			return ITEM_URL + matcher.group(1);
		return "";
	}

	private String extractCategoryLink(Element categoryLink) {
		Matcher matcher = CATEGORY_PATTERN.matcher(categoryLink.attr("onclick"));
		if (matcher.matches())
			return CATEGORY_URL + matcher.group(2);
		return "";
	}

}
