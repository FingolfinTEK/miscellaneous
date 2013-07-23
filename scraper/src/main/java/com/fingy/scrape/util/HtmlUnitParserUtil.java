package com.fingy.scrape.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitParserUtil {

    public static Document getPageFromUrl(String url) throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
        HtmlPage page = webClient.getPage(url);
        Document parsedPage = Jsoup.parse(page.asXml());
        webClient.closeAllWindows();
        return parsedPage;
    }
}
