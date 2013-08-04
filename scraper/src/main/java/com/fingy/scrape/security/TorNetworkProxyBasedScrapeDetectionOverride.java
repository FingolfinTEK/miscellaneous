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
        // TODO Auto-generated method stub

    }

    @Override
    public void tearDownProxy() {
        TorUtil.stopTor();

    }

}
