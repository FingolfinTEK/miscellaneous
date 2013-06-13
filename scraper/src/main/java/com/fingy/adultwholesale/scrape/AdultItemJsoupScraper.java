package com.fingy.adultwholesale.scrape;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;

public class AdultItemJsoupScraper extends AbstractJsoupScraper<AdultItem> {

	public AdultItemJsoupScraper(String scrapeUrl) {
		super(scrapeUrl);
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
		final String imageUrl = scrapeImageUrlFromPage(page);
		
		return new AdultItem(id, title, category, price, stockStatus, description, productUrl, imageUrl);
		
	}

	private String scrapeIdFromPage(Document page) {
		return page.select("ul.instock li").first().text().replace("Model: ", "");
	}

	private String scrapeTitleFromPage(Document page) {
		return page.select("div.name-type").first().text();
	}

	private String scrapeCategoryFromPage(Document page) {
		final Elements navigation = page.select("#navBreadCrumb a");
		final List<Element> onlyCategories = navigation.subList(1, navigation.size());
		
		String itemCategories = "";
		for(Element category: onlyCategories) 
			itemCategories += ">" + category.text();

		return itemCategories.replaceFirst(">", "");
	}

	private String scrapePriceFromPage(Document page) {
		return "full access required";
	}

	private String scrapeStockStatusFromPage(Document page) {
		return "full access required";
	}

	private String scrapeDescriptionFromPage(Document page) {
		return page.getElementById("productDescription").text();
	}

	private String scrapeImageUrlFromPage(Document page) {
		return "http://adultwholesaledirect.com/tour/" + page.select("#productMainImage img").first().attr("src");
	}

}
