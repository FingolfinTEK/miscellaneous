package com.fingy.aprod.scrape;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fingy.aprod.Contact;
import com.fingy.scrape.exception.ScrapeException;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.jsoup.HttpClientParserUtil;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class ContactJsoupScraper extends AbstractAprodJsoupScraper<Contact> {

	private static final Pattern PHONE_ID_REGEX = Pattern.compile(".+'id':'(\\w+)'.+");

	public ContactJsoupScraper(String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(Collections.<String, String> emptyMap(), scrapeUrl, linksQueue);
	}

	public ContactJsoupScraper(Map<String, String> cookies, String scrapeUrl, ScraperLinksQueue linksQueue) {
		super(cookies, scrapeUrl, linksQueue);
	}

	@Override
	protected Document getPage(String scrapeUrl) throws IOException {
		try {
			return HttpClientParserUtil.getPageFromUrl(scrapeUrl);
		} catch (IOException e) {
			AbstractAprodJsoupScraper.setSessionExpired(true);
			throw e;
		}
	}

	@Override
	protected Contact scrapePage(Document page) {
		String category = scrapeCategoryFromPage(page);
		String name = scrapeNameFromPage(page);
		String phoneNumber = scrapePhoneNumberFromPage(page);

		if (isValidNumber(phoneNumber)) {
			linksQueue.markVisited(getScrapeUrl());
			return new Contact(category, name, phoneNumber);
		}

		AbstractJsoupScraper.setSessionExpired(true);
		linksQueue.addIfNotVisited(getScrapeUrl());
		throw new ScrapeException("Session expired!");
	}

	private String scrapeCategoryFromPage(Document page) {
		return getTagTextFromCssQuery(page, "div.offerhead table.breadcrumb li:last-child a").trim();
	}

	private String scrapeNameFromPage(Document page) {
		return getTagTextFromCssQuery(page, "#ad_active.content div.userbox p.x-large2 span.block").trim();
	}

	private String scrapePhoneNumberFromPage(Document page) {
		String phoneNumber = "N/A";
		Elements phoneLinks = page.select("#contact_methods div.overh a");
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
			String phoneNumberUrl = "http://aprod.hu/ajax/misc/contact/phone/" + requestId + "/";
			String phoneNumberString = HttpClientParserUtil.getPageAsStringFromUrl(phoneNumberUrl);
			return cleanPhoneNumber(phoneNumberString);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "N/A";
	}

	private String cleanPhoneNumber(String phoneNumberString) {
		String extractedPhoneNumbers = extractMultiplePhoneNumbers(phoneNumberString);
		String cleanedPhoneNumbers = removeIllegalCharacters(extractedPhoneNumbers);
		return separateMultiplePhoneNumbersBySpaceInsteadOfDash(cleanedPhoneNumbers);
	}

	private String extractMultiplePhoneNumbers(String phoneNumberString) {
		return phoneNumberString.replaceAll("<\\/span>*<span class=\\block\\>", ",").replaceAll("<span class=\\block\\>", "").replaceAll("<\\/span>", "");
	}

	private String removeIllegalCharacters(String phoneNumberString) {
		return phoneNumberString.replaceAll("[ \\-\"]+", "");
	}

	private String separateMultiplePhoneNumbersBySpaceInsteadOfDash(String phoneNumberString) {
		return phoneNumberString.replaceAll(",", " ");
	}

	private boolean isValidNumber(String phoneNumber) {
		return !phoneNumber.contains("limitet");
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new ContactJsoupScraper(
				"http://aprod.hu/hirdetes/budapestxvii-fouton-elado-egy-azonnal-bekoltozheto-ikerhaz-resz-ID16BcJ.html#aab5e98961;promoted",
				new ScraperLinksQueue()).call());
	}
}
