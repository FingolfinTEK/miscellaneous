package com.fingy.scrape.jsoup;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fingy.scrape.security.TrustAllCertificates;

public class HttpClientParserUtil {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

	private static HttpClient httpClient = getNewHttpClient();

	public static void resetClient() {
		httpClient = getNewHttpClient();
	}

	public static HttpClient getNewHttpClient() {
		try {
			TrustManager tm = new TrustAllCertificates();
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { tm }, null);

			SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			registry.register(new Scheme("https", 443, sf));

			ClientConnectionManager manager = new ThreadSafeClientConnManager(registry);
			DefaultHttpClient defaultHttpClient = new DefaultHttpClient(manager);
			addProxyIfNeeded(defaultHttpClient);
			return defaultHttpClient;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
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
		return Jsoup.parse(entity.getContent(), "UTF-8", "");
	}

	private static HttpEntity getEntityFromUrl(String scrapeUrl) throws IOException, ClientProtocolException {
		HttpGet get = new HttpGet(scrapeUrl);
		get.setHeader("User-Agent", USER_AGENT);
		HttpResponse response = httpClient.execute(get);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
