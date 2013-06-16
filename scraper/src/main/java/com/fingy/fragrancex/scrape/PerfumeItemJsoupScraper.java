/**
 * 
 */
package com.fingy.fragrancex.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fingy.fragrancex.PerfumeItem;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;

public class PerfumeItemJsoupScraper extends AbstractJsoupScraper<PerfumeItem> {

	private static final String NOIMAGE2_URL = "http://www.fragrancex.com/images/noimage2.png";
	private static final String SEARCH_URL = "http://www.fragrancex.com/search/search_results?stext=";
	private static final String BASE_URL = "http://www.fragrancex.com/images/products/sku/large/";
	private static final Pattern IMAGE_PATTERN = Pattern
			.compile("javascript: viewimagepopUp\\('/products/image\\.html\\?sid=(.+)'\\);?");

	private final FragrancexTask task;

	public PerfumeItemJsoupScraper(FragrancexTask task) {
		super(SEARCH_URL + task.getId());
		this.task = task;
	}

	public FragrancexTask getTask() {
		return task;
	}

	@Override
	protected PerfumeItem scrapePage(Document page) {
		final String name = scrapeNameFromPage(page);
		final String description = scrapeDescriptionFromPage(page);
		final String price = scrapePriceFromPage(page);
		final String imageUrl = scrapeImageUrlFromPage(page);

		return new PerfumeItem(task.getId(), name, description, price, imageUrl, task.getRow());
	}

	private String scrapeNameFromPage(Document page) {
		return getTagTextFromCssQuery(page, "div.in.product-hero-scale h1.h2");
	}

	private String scrapeDescriptionFromPage(Document page) {
		return getTagTextFromCssQuery(page, "div.in.product-hero-scale p.mtn");
	}

	private String scrapePriceFromPage(Document page) {
		Elements product = page.getElementsContainingText(task.getSizeOrType());
		return product.isEmpty() ? "N/A" : getTagTextFromCssQuery(product.first(), "p.new-price.sans.mtn.pbn");
	}

	private String scrapeImageUrlFromPage(Document page) {
		String imageLink = getImageHrefAttributeFromPage(page);
		return extractLinkFromHref(imageLink);
	}

	private String extractLinkFromHref(String imageLink) {
		Matcher matcher = IMAGE_PATTERN.matcher(imageLink);
		if (matcher.matches())
			return BASE_URL + matcher.group(1) + ".jpg";
		return NOIMAGE2_URL;
	}

	private String getImageHrefAttributeFromPage(Document page) {
		Elements imageElems = page.select("a.fs11.mq1hide img[alt*=" + task.getSizeOrType() + "]");
		return imageElems.isEmpty() ? "" : imageElems.first().parent().attr("href");
	}

	public static void main(String[] args) {
		String id = "498159";
		String sizeOrType = "1.7 oz Eau De Parfum Spray";
		PerfumeItem perfume = new PerfumeItemJsoupScraper(new FragrancexTask(0, id, sizeOrType)).call();
		System.out.println(perfume);
	}
}
