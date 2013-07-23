package com.fingy.proxylist;

public class ProxyInfo {

    private final String host;
    private final String port;
    private final ProxyType type;

    public ProxyInfo(String host, String port, ProxyType type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public ProxyType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(host);
        builder.append(":");
        builder.append(port);
        return builder.toString();
    }

}
