package com.fingy.scrape.security.util;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.fingy.scrape.security.TrustAllCertificates;

public class CertificateUtil {

	public static void trustAllCertificates() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllCertificates() };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
