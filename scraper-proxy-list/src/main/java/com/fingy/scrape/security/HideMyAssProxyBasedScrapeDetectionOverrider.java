package com.fingy.scrape.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.proxylist.ProxyInfo;
import com.fingy.proxylist.ProxyListScraperScheduler;
import com.fingy.proxylist.ProxyType;

public class HideMyAssProxyBasedScrapeDetectionOverrider implements ProxyBasedScrapeDetectionOverrider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Integer currentIndex;
    private List<ProxyInfo> proxies;

    public List<ProxyInfo> getProxies() {
        return proxies;
    }

    @Override
    public void initializeContext() {
        try {
            currentIndex = 0;
            proxies = Collections.synchronizedList(scrapeProxies());
            Collections.shuffle(proxies);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error loading proxies");
        }
    }

    protected List<ProxyInfo> scrapeProxies() throws InterruptedException, ExecutionException {
        return new ProxyListScraperScheduler().getProxies();
    }

    @Override
    public void setUpProxy() {
        ProxyInfo proxy = proxies.get(currentIndex++);
        ProxyType type = proxy.getType();
        currentIndex = currentIndex % proxies.size();

        System.setProperty(type.getHostPropertyName(), proxy.getHost());
        System.setProperty(type.getPortPropertyName(), proxy.getPort());

    }

    @Override
    public void destroyContext() {
    }

    protected void setProxies(final Set<ProxyInfo> uniqueProxies) {
        synchronized (proxies) {
            proxies.clear();
            proxies.addAll(uniqueProxies);
        }
    }

}
