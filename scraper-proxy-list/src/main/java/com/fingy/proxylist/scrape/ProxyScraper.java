package com.fingy.proxylist.scrape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fingy.proxylist.ProxyInfo;
import com.fingy.proxylist.ProxyType;
import com.fingy.scrape.AbstractScraper;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ProxyScraper extends AbstractScraper<Collection<ProxyInfo>> {

    public ProxyScraper(String scrapeUrl) {
        super(scrapeUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<ProxyInfo> scrapeLink() {
        List<ProxyInfo> proxies = new ArrayList<>();

        try {
            WebClient webClient = new WebClient();
            HtmlPage page = webClient.getPage(getScrapeUrl());
            List<DomElement> rows = (List<DomElement>) page.getByXPath("/html/body/div/div/table/tbody/tr");
            for (DomElement domElement : rows) {
                DomNodeList<DomNode> columns = domElement.getChildNodes();

                if ("http".equalsIgnoreCase(columns.get(6).asText())) {
                    String host = StringUtils.deleteWhitespace(columns.get(1).asText());
                    String port = columns.get(2).asText();
                    proxies.add(new ProxyInfo(host, port, ProxyType.HTTP_PROXY));
                }
            }
            webClient.closeAllWindows();
        } catch (FailingHttpStatusCodeException | IOException e) {
            e.printStackTrace();
        }
        return proxies;
    }

}
