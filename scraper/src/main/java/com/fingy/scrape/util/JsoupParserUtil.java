package com.fingy.scrape.util;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupParserUtil {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

	public static Document getPageFromUrl(String url) throws IOException {
		return getPageFromUrlWithTimeout(url, 0);
	}

	public static Response getResponseFromUrl(String url) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).ignoreContentType(true).execute();
	}

	public static String getResponseBodyAsTextFromUrl(String url) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).ignoreContentType(true).execute().body();
	}

	public static String getResponseBodyAsTextFromUrlWithCookies(String url, Map<String, String> cookies) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).timeout(0).ignoreContentType(true).execute().body();
	}

	public static Document postDataToUrlWithCookies(String url, Map<String, String> cookies, Map<String, String> params) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).timeout(0).ignoreContentType(true).data(params).post();
	}

	public static Document getPageFromUrlWithCookies(String url, Map<String, String> cookies) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).timeout(0).get();
	}

	public static Document getPageFromUrlWithTimeout(String url, int timeout) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(timeout).get();
	}

	public static String getTagTextFromCssQuery(Element elementToQuery, String cssQuery) {
		Elements element = elementToQuery.select(cssQuery);
		return element.isEmpty() ? "N/A" : element.first().text().trim();
	}

	public static Element getTagFromCssQuery(Element elementToQuery, String cssQuery) {
		Elements element = elementToQuery.select(cssQuery);
		return element.isEmpty() ? null : element.first();
	}

	public static Document smartGetPageFromUrl(String url) throws IOException {
		return smartGetPageFromUrlWithTimeout(url, 0);
	}

	public static Document smartGetPageFromUrlWithTimeout(String url, int timeout) throws IOException {
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).timeout(timeout).get();
		loadScriptsFromPage(page);
		loadImagesFromPage(page);
		loadLinkTagsFromPage(page);
		return page;
	}

	private static void loadScriptsFromPage(Document page) throws IOException {
		for (Element script : page.select("script")) {
			accessUrlIfNotBlank(script.attr("abs:src"));
		}
	}

	private static void accessUrlIfNotBlank(String url) throws IOException {
		if (StringUtils.isNotBlank(url))
			accessUrlIfNotBlank(url);
	}

	private static void loadImagesFromPage(Document page) throws IOException {
		for (Element script : page.select("img")) {
			accessUrlIfNotBlank(script.attr("abs:src"));
		}
	}

	private static void loadLinkTagsFromPage(Document page) throws IOException {
		for (Element script : page.select("link")) {
			accessUrlIfNotBlank(script.attr("abs:href"));
		}
	}

}
