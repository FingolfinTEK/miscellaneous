package com.fingy.scrape.security;

import com.fingy.scrape.security.util.TorUtil;

public class TorNetworkProxyBasedScrapeDetectionOverride implements ProxyBasedScrapeDetectionOverrider {

    @Override
    public void initializeContext() {
        TorUtil.stopTor();
        TorUtil.startAndUseTorAsProxy();
    }

    @Override
    public void setUpProxy() {
        TorUtil.requestNewIdentity();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
    }

    @Override
    public void destroyContext() {
        TorUtil.stopTor();
    }

}
