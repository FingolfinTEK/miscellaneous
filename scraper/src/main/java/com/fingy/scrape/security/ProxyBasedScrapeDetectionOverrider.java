package com.fingy.scrape.security;

public interface ProxyBasedScrapeDetectionOverrider {

    void initializeContext();

    void setUpProxy();

    void destroyContext();

}
