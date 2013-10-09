package com.fingy.scrape.security;

public class TorNetworkProxyBasedRotatingScrapeDetectionOverride extends TorNetworkProxyBasedScrapeDetectionOverride {

    private static final int DEFAULT_TIMEOUT_MILLIS = 180000;
    private final Thread proxyRefreshingThread;

    public TorNetworkProxyBasedRotatingScrapeDetectionOverride() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    public TorNetworkProxyBasedRotatingScrapeDetectionOverride(final int timeoutMillis) {
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
        private final int timeoutMillis;

        public ProxyRefreshingThread(final int timeoutMillis) {
            setDaemon(true);
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public void run() {
            try {
                doRun();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void doRun() throws InterruptedException {
            while (!interrupted()) {
                sleep(DEFAULT_TIMEOUT_MILLIS);
                setUpProxy();
            }
        }
    }

}
