package com.fingy.proxylist;

import static com.fingy.scrape.security.ProxyConstants.HTTPS_PROXY_HOST_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.HTTPS_PROXY_PORT_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.HTTP_PROXY_HOST_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.HTTP_PROXY_PORT_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.SOCKS_PROXY_HOST_PROPERTY_NAME;
import static com.fingy.scrape.security.ProxyConstants.SOCKS_PROXY_PORT_PROPERTY_NAME;

public enum ProxyType {

    HTTP_PROXY(HTTP_PROXY_HOST_PROPERTY_NAME, HTTP_PROXY_PORT_PROPERTY_NAME), HTTPS_PROXY(HTTPS_PROXY_HOST_PROPERTY_NAME,
            HTTPS_PROXY_PORT_PROPERTY_NAME), SOCKS_PROXY(SOCKS_PROXY_HOST_PROPERTY_NAME, SOCKS_PROXY_PORT_PROPERTY_NAME);

    private final String hostPropertyName;
    private final String portPropertyName;

    private ProxyType(final String hostPropertyName, final String portPropertyName) {
        this.hostPropertyName = hostPropertyName;
        this.portPropertyName = portPropertyName;
    }

    public String getHostPropertyName() {
        return hostPropertyName;
    }

    public String getPortPropertyName() {
        return portPropertyName;
    }

}
