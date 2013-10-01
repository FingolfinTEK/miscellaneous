package com.fingy.scrape.util;

import com.fingy.io.IOHelper;
import com.fingy.scrape.security.TrustAllCertificates;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.TruncatedChunkException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpClientParserUtil {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final int EOF = -1;

    private static PoolingClientConnectionManager manager = createPoolingClientConnectionManager();
    private static ThreadLocal<DefaultHttpClient> httpClient = new ThreadLocal<DefaultHttpClient>() {
        @Override
        protected DefaultHttpClient initialValue() {
            return getNewHttpClient();
        }
    };

    public static DefaultHttpClient getNewHttpClient() {
        try {
            return createDefaultHttpClient(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DefaultHttpClient();
    }

    private static PoolingClientConnectionManager createPoolingClientConnectionManager() {
        try {
            TrustManager tm = new TrustAllCertificates();
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);

            SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            registry.register(new Scheme("https", 443, sf));

            PoolingClientConnectionManager manager = new PoolingClientConnectionManager(registry);
            manager.setDefaultMaxPerRoute(10);
            return manager;
        } catch (NoSuchAlgorithmException | KeyManagementException ignored) {
        }

        return new PoolingClientConnectionManager();

    }

    private static DefaultHttpClient createDefaultHttpClient(PoolingClientConnectionManager manager) {
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(manager);
        addProxyIfNeeded(defaultHttpClient);
        return defaultHttpClient;
    }

    private static void addProxyIfNeeded(DefaultHttpClient defaultHttpClient) {
        String proxyPort = System.getProperty("http.proxyPort");
        String proxyHost = System.getProperty("http.proxyHost");

        if (proxyPort != null && proxyHost != null) {
            HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
            defaultHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    public static Document getPageFromUrl(String scrapeUrl) throws IOException, ClientProtocolException {
        HttpEntity entity = getEntityFromUrl(scrapeUrl);
        final byte[] content = IOHelper.readContent(entity.getContent(), TruncatedChunkException.class);
        return Jsoup.parse(new String(content, "UTF-8"), "");
    }

    private static HttpEntity getEntityFromUrl(String scrapeUrl) throws IOException, ClientProtocolException {
        HttpGet get = new HttpGet(scrapeUrl);
        get.setHeader("User-Agent", USER_AGENT);
        HttpResponse response = httpClient.get().execute(get);
        HttpEntity entity = response.getEntity();
        return entity;
    }

    public static String getPageAsStringFromUrl(String scrapeUrl) throws IOException, ClientProtocolException {
        HttpEntity entity = getEntityFromUrl(scrapeUrl);
        return IOUtils.toString(entity.getContent());
    }

    public static String delayedGetPageAsStringFromUrl(long delayMillis, String scrapeUrl) throws IOException, ClientProtocolException {
        delay(delayMillis);
        HttpEntity entity = getEntityFromUrl(scrapeUrl);
        return IOUtils.toString(entity.getContent());
    }

    private static void delay(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String postDataToUrlWithCookies(String searchUrl, String data, Map<String, String> cookies) throws IOException {
        HttpPost post = new HttpPost(searchUrl);
        post.setHeader("User-Agent", USER_AGENT);
        post.setEntity(new StringEntity(data, ContentType.create("application/json", "UTF-8")));

        DefaultHttpClient client = httpClient.get();
        CookieStore cookieStore = client.getCookieStore();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            cookieStore.addCookie(new BasicClientCookie(entry.getKey(), entry.getValue()));
        }
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        final byte[] content = IOHelper.readContent(entity.getContent(), TruncatedChunkException.class);
        return new String(content, "windows-1255");
    }
}
