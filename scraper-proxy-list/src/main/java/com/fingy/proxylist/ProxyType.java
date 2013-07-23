package com.fingy.proxylist;

public enum ProxyType {

    HTTP_PROXY("http.proxyHost", "http.proxyPort"), HTTPS_PROXY("https.proxyHost", "https.proxyPort"), SOCKS_PROXY("socksProxyHost",
            "socksProxyPort");

    private final String hostPropertyName;
    private final String portPropertyName;

    private ProxyType(String hostPropertyName, String portPropertyName) {
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
