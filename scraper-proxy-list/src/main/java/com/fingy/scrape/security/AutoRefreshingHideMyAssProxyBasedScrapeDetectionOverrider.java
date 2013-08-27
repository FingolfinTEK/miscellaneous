package com.fingy.scrape.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.fingy.proxylist.ProxyInfo;

public class AutoRefreshingHideMyAssProxyBasedScrapeDetectionOverrider extends HideMyAssProxyBasedScrapeDetectionOverrider {

    private final Thread proxyRefreshingThread = new ProxyRefreshingThread();

    @Override
    public void initializeContext() {
        super.initializeContext();
        proxyRefreshingThread.start();
    }

    @Override
    public void destroyContext() {
        proxyRefreshingThread.interrupt();
        super.destroyContext();
    }

    private class ProxyRefreshingThread extends Thread {
        private static final int DEFAULT_TIMEOUT_MILLIS = 1800000;

        public ProxyRefreshingThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!interrupted()) {
                    doRun();
                }
            } catch (InterruptedException e) {
            }
        }

        public void doRun() throws InterruptedException {
            try {
                sleep(DEFAULT_TIMEOUT_MILLIS);
                List<ProxyInfo> proxies = getProxies();
                Set<ProxyInfo> uniqueProxies = new HashSet<>(proxies);
                uniqueProxies.addAll(scrapeProxies());

                setProxies(uniqueProxies);
            } catch (ExecutionException e) {
            }
        }
    }

}
