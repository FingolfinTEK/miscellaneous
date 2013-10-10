package com.fingy.adultwholesale.scrape;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.context.ScraperLinksQueue;

public class AdultItemCategoryJsoupScraper extends AbstractAdultItemJsoupScraper {

	private static final String ITEM_URL = "http://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkproductdetails.php?prod_id=";
	private static final String CATEGORY_URL = "http://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkproducts.php?categ_id=%s&start=%s&browseby=1&displayType=1&rppt=96";

	private static final Pattern ITEM_PATTERN = Pattern.compile("javascript: void\\(displayDetails\\((\\d+)\\)\\)");
	private static final Pattern PAGE_PATTERN = Pattern.compile("javascript: void\\(changeStart\\((\\d+)\\)\\)");
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("javascript: void\\((changeCateg|toggleCateg)\\((\\d+)\\)\\)");
	private static final Pattern CATEGORY_ID_PATERN = Pattern.compile(".+categ_id=(\\d+)&.+");

	public AdultItemCategoryJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl, linksQueue);
	}

	@Override
	protected AdultItem doScrapePage(Document page) {
		scrapePages(page);
		scrapeCategories(page);
		scrapeSubCategories(page);
		scrapeNextPage(page);

		linksQueue.markVisited(getScrapeUrl());
		return new AdultItem("", "", "", "", "", "", "", getScrapeUrl(), "");
	}

	private void scrapePages(Document page) {
		final Elements itemLinks = page.select("div.itemsList div.item span.title a");
		for (Element itemLink : itemLinks) {
			String link = extractItemLink(itemLink.attr("onclick"));
			linksQueue.addIfNotVisited(link);
		}
	}

	private String extractItemLink(String itemLink) {
		Matcher matcher = ITEM_PATTERN.matcher(itemLink);
		if (matcher.matches())
			return ITEM_URL + matcher.group(1);
		return "";
	}

	private void scrapeCategories(Document page) {
		final Elements categoryLinks = page.select("a.categorylinkred");
		for (Element categoryLink : categoryLinks) {
			String link = extractCategoryLink(categoryLink.attr("onclick"));
			linksQueue.addIfNotVisited(link);
		}
	}

	private void scrapeSubCategories(Document page) {
		final Elements categoryLinks = page.select("ul.categTree li a");
		for (Element categoryLink : categoryLinks) {
			String link;
			if (!categoryLink.attr("onclick").equals("")) {
				link = extractCategoryLink(categoryLink.attr("onclick"));
			} else {
				link = extractCategoryLink(categoryLink.attr("href"));
			}
			linksQueue.addIfNotVisited(link);
		}
	}

	private String extractCategoryLink(String categoryLink) {
		Matcher matcher = CATEGORY_PATTERN.matcher(categoryLink);
		if (matcher.matches())
			return String.format(CATEGORY_URL, matcher.group(2), 0);
		return "";
	}

	private void scrapeNextPage(Document page) {
		final Elements pageLinks = page.select("div.pag a");

		for (Element linkElement : pageLinks) {
			String link = extractNextLink(linkElement.attr("href"));
			linksQueue.addIfNotVisited(link);
		}
	}

	private String extractNextLink(String categoryLink) {
		Matcher nextPageMatcher = PAGE_PATTERN.matcher(categoryLink);
		if (nextPageMatcher.matches()) {
			Matcher categoryIdMatcher = CATEGORY_ID_PATERN.matcher(getScrapeUrl());
			String id = categoryIdMatcher.matches() ? categoryIdMatcher.group(1) : "0";
			return String.format(CATEGORY_URL, id, nextPageMatcher.group(1));
		}

		return "";
	}

}
