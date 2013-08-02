package com.fingy.yellowpages.scrape;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.util.JsoupParserUtil;
import com.fingy.yellowpages.CompanyDetails;

public class CompanyDetailsScraper extends AbstractJsoupScraper<CompanyDetails> {

	public CompanyDetailsScraper(String scrapeUrl) {
		super(scrapeUrl);
	}

	@Override
	protected CompanyDetails scrapePage(Document page) {
		final String name = parseNameFromPage(page);
		final String address = parseAddressFromPage(page);
		final String phone = parsePhoneNumberFromPage(page);
		final String website = parseWebsiteFromPage(page);
		final String email = parseEmailFromPage(page);
		return new CompanyDetails(name, address, phone, website, email);
	}

	private String parseNameFromPage(Document page) {
		return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard h1.fn a.url");
	}

	private String parseAddressFromPage(Document page) {
		return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard p.primary-location span.listing-address");
	}

	private String parsePhoneNumberFromPage(Document page) {
		return JsoupParserUtil.getTagTextFromCssQuery(page, "div#basic-info div.vcard p.phone");
	}

	private String parseWebsiteFromPage(Document page) {
		Element emailTag = JsoupParserUtil.getTagFromCssQuery(page, "div#basic-info div.vcard a.primary-website");
		return emailTag == null ? "N/A" : emailTag.attr("href");
	}

	private String parseEmailFromPage(Document page) {
		Element emailTag = JsoupParserUtil.getTagFromCssQuery(page, "div#basic-info div.vcard a.track-email-business");
		return emailTag == null ? "N/A" : emailTag.attr("href").replace("mailto:", "");
	}

}
