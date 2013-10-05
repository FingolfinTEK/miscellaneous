package com.fingy.aprod.scrape;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fingy.scrape.util.HtmlUnitParserUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.aprod.Contact;
import com.fingy.aprod.scrape.exception.SessionExpiredException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.util.JsoupParserUtil;

public class ContactJsoupScraper extends AbstractAprodHuJsoupScraper<Contact> {

	private static final String NOT_AVAILABLE = "N/A";
	private static final String PHONE_NUMBER_URL_FORMAT = "http://aprod.hu/ajax/misc/contact/phone/%s/";
	private static final Pattern PHONE_ID_REGEX = Pattern.compile(".+'id':'(\\w+)'.+");

	public ContactJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl, Collections.<String, String> emptyMap(), linksQueue);
	}

	public ContactJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(scrapeUrl, cookies, linksQueue);
	}

	@Override
	protected Contact scrapePage(Document page) {
		String category = scrapeCategoryFromPage(page);
		String name = scrapeNameFromPage(page);
		String phoneNumber = scrapePhoneNumberFromPage(page);

		if (shouldNotExpireSession(phoneNumber)) {
			getLinksQueue().markVisited(getScrapeUrl());
			return new Contact(category, name, phoneNumber);
		}

		getLinksQueue().addIfNotVisited(getScrapeUrl());
		AbstractJsoupScraper.setScrapeCompromised(true);
		throw new SessionExpiredException(getScrapeUrl());
	}

	private String scrapeCategoryFromPage(Document page) {
		return JsoupParserUtil.getTagTextFromCssQuery(page, "div.offerhead table.breadcrumb li:last-child a").trim();
	}

	private String scrapeNameFromPage(Document page) {
		return JsoupParserUtil.getTagTextFromCssQuery(page, "#ad_active p.userdetails span.block").trim();
	}

	private String scrapePhoneNumberFromPage(Document page) {
		String phoneNumber = NOT_AVAILABLE;
		Elements phoneLinks = page.select("#contact_methods a.link-phone");
		if (!phoneLinks.isEmpty()) {
			Element phoneLink = phoneLinks.first();
			phoneNumber = processPhoneLink(phoneNumber, phoneLink);
		}
		return phoneNumber;
	}

	private String processPhoneLink(String phoneNumber, Element phoneLink) {
		Matcher matcher = PHONE_ID_REGEX.matcher(phoneLink.className());
		if (matcher.matches()) {
			String requestId = matcher.group(1);
			phoneNumber = getPhoneNumberByAjax(requestId);
		}
		return phoneNumber;
	}

	private String getPhoneNumberByAjax(String requestId) {
		try {
			String phoneNumberUrl = String.format(PHONE_NUMBER_URL_FORMAT, requestId);
			String phoneNumberString = HtmlUnitParserUtil.getPageFromUrlWithoutJavaScriptSupportUsingClient(getWebClient(), phoneNumberUrl).text();
			return cleanPhoneNumber(phoneNumberString);
		} catch (IOException e) {
			logger.error("Exception scraping phone number", e);
		}

		return NOT_AVAILABLE;
	}

	private String cleanPhoneNumber(String phoneNumberString) {
		String extractedPhoneNumbers = extractMultiplePhoneNumbers(phoneNumberString);
		String cleanedPhoneNumbers = removeIllegalCharacters(extractedPhoneNumbers);
		return separateMultiplePhoneNumbersBySpaceInsteadOfComma(cleanedPhoneNumbers);
	}

	private String extractMultiplePhoneNumbers(String phoneNumberString) {
		return phoneNumberString.replace("<\\/span> <span class=\\\"block\\\">", ",").replace("<span class=\\\"block\\\">", "").replace("<\\/span>", "");
	}

	private String removeIllegalCharacters(String phoneNumberString) {
		return phoneNumberString.replaceAll("[ \\-\"]+", "").trim();
	}

	private String separateMultiplePhoneNumbersBySpaceInsteadOfComma(String phoneNumberString) {
		return phoneNumberString.replaceAll(",", " ");
	}

	private boolean shouldNotExpireSession(String phoneNumber) {
		return !phoneNumber.contains("limitet");
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new ContactJsoupScraper("http://aprod.hu/hirdetes/dohanyboltnak-uzlethelyiseg-kiado-benzinkuton-ID13WEp.html#aab5e98961", new ScraperLinksQueue()).call());
	}
}
