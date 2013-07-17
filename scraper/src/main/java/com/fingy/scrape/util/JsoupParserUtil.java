package com.fingy.scrape.util;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupParserUtil {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

	public static Document getPageFromUrl(String url) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).get();
	}

	public static Response getResponseFromUrl(String url) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).ignoreContentType(true).execute();
	}

	public static String getResponseBodyAsTextFromUrl(String url) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).ignoreContentType(true).execute().body();
	}

	public static Document getPageFromUrl(String url, Map<String, String> cookies) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).timeout(0).get();
	}

	public static String getTagTextFromCssQuery(Element elementToQuery, String cssQuery) {
		Elements element = elementToQuery.select(cssQuery);
		return element.isEmpty() ? "N/A" : element.first().text().trim();
	}

	public static Element getTagFromCssQuery(Element elementToQuery, String cssQuery) {
		Elements element = elementToQuery.select(cssQuery);
		return element.isEmpty() ? null : element.first();
	}
}
