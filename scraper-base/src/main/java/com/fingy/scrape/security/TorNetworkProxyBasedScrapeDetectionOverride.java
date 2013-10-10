package com.fingy.scrape.security;

import com.fingy.scrape.security.util.TorUtil;

public class TorNetworkProxyBasedScrapeDetectionOverride implements ProxyBasedScrapeDetectionOverrider {

    @Override
    public void initializeContext() {
        TorUtil.stopTor();
        TorUtil.startAndUseTorAsProxy();
        sleep(45000);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {}
    }

    @Override
    public void setUpProxy() {
        TorUtil.requestNewIdentity();
        sleep(1000);
    }

    @Override
    public void destroyContext() {
        TorUtil.stopTor();
    }

}
