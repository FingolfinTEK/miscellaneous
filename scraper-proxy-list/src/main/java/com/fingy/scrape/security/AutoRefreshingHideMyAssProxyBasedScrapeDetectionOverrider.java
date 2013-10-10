package com.fingy.scrape.security;

import com.fingy.proxylist.ProxyInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class AutoRefreshingHideMyAssProxyBasedScrapeDetectionOverrider extends HideMyAssProxyBasedScrapeDetectionOverrider {

    private final Thread proxyRefreshingThread;

    public AutoRefreshingHideMyAssProxyBasedScrapeDetectionOverrider() {
        proxyRefreshingThread = new ProxyRefreshingThread();
    }

    public AutoRefreshingHideMyAssProxyBasedScrapeDetectionOverrider(final long timeoutMillis) {
        proxyRefreshingThread = new ProxyRefreshingThread(timeoutMillis);
    }

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
        private static final long DEFAULT_TIMEOUT_MILLIS = 1800000;

        private final long timeoutMillis;

        public ProxyRefreshingThread() {
            this(DEFAULT_TIMEOUT_MILLIS);
        }

        public ProxyRefreshingThread(final long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
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
                sleep(timeoutMillis);
                List<ProxyInfo> proxies = getProxies();
                Set<ProxyInfo> uniqueProxies = new HashSet<>(proxies);
                uniqueProxies.addAll(scrapeProxies());

                setProxies(uniqueProxies);
            } catch (ExecutionException e) {
            }
        }
    }

}
