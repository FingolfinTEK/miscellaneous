package com.fingy.scrape.util;

import static com.fingy.scrape.security.ProxyConstants.HTTP_PROXY_HOST_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.HTTP_PROXY_PORT_PROPERTY_NAME;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitParserUtil {

    private static final int DEFAULT_TIMEOUT = 30000;

    public static Document getPageFromUrlWithoutJavaScriptSupport(final String url) throws IOException {
        WebClient webClient = getWebClientWithProxyEnabledIfNeeded();
        return getHtmlPageFromUrlWithoutJavaScriptSupportUsingClient(webClient, url);
    }

    public static Document getHtmlPageFromUrlWithoutJavaScriptSupportUsingClient(WebClient webClient, String url) throws IOException {
        boolean isJavaScriptEnabled = webClient.getOptions().isJavaScriptEnabled();
        webClient.getOptions().setJavaScriptEnabled(false);

        Document parsedPage = getHtmlPageFromUrlUsingClient(webClient, url);

        webClient.closeAllWindows();
        webClient.getOptions().setJavaScriptEnabled(isJavaScriptEnabled);
        return parsedPage;
    }

    public static Document getPageFromUrlWithoutJavaScriptSupportUsingClient(WebClient webClient, String url) throws IOException {
        boolean isJavaScriptEnabled = webClient.getOptions().isJavaScriptEnabled();
        webClient.getOptions().setJavaScriptEnabled(false);

        Document parsedPage = getPageFromUrlUsingClient(webClient, url);

        webClient.closeAllWindows();
        webClient.getOptions().setJavaScriptEnabled(isJavaScriptEnabled);
        return parsedPage;
    }

    public static WebClient getWebClientWithProxyEnabledIfNeeded() {
        if (isHttpProxySet()) {
            String host = System.getProperty(HTTP_PROXY_HOST_PROPERTY_NAME);
            String port = System.getProperty(HTTP_PROXY_PORT_PROPERTY_NAME);
            return new WebClient(BrowserVersion.FIREFOX_17, host, Integer.parseInt(port));
        }

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
        webClient.getOptions().setTimeout(DEFAULT_TIMEOUT);
        return webClient;
    }

    private static boolean isHttpProxySet() {
        return System.getProperty(HTTP_PROXY_HOST_PROPERTY_NAME) != null;
    }

    public static Document getHtmlPageFromUrlUsingClient(final WebClient webClient, final String url) throws IOException {
        HtmlPage page = webClient.getPage(url);
        return Jsoup.parse(page.asXml());
    }

    public static Document getPageFromUrlUsingClient(final WebClient webClient, final String url) throws IOException {
        Page page = webClient.getPage(url);
        return Jsoup.parse(page.getWebResponse().getContentAsString());
    }
}
