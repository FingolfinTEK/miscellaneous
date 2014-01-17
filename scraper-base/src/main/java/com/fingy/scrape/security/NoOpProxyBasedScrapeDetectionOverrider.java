package com.fingy.scrape.security;

public class NoOpProxyBasedScrapeDetectionOverrider implements ProxyBasedScrapeDetectionOverrider {

    @Override
    public void initializeContext() {
    }

    @Override
    public void setUpProxy() {
    }

    @Override
    public void destroyContext() {
    }
}
