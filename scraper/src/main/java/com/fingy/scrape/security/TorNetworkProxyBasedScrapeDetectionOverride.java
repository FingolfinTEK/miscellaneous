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
    }

    @Override
    public void destroyContext() {
        TorUtil.stopTor();
    }

}
