package com.fingy.micromedex.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;

public class HtmlUnitUtil {
	public static final String START_URL = "http://www.micromedexsolutions.com/home/dispatch";
	public static final String USERNAME = "TRI1";
	public static final String PASSWORD = "33tybs@h";

	public static Map<String, String> getCookiesAsMap(WebClient client) {
		Map<String, String> cookiesAsMap = new HashMap<>();

		for (Cookie cookie : client.getCookieManager().getCookies()) {
			cookiesAsMap.put(cookie.getName(), cookie.getValue());
		}

		return cookiesAsMap;
	}

	public static void doLogin(final WebClient webClient) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		boolean javaScriptEnabled = webClient.getOptions().isJavaScriptEnabled();
		webClient.getOptions().setJavaScriptEnabled(false);

		HtmlPage page = webClient.getPage(START_URL);

		HtmlTextInput usernameField = (HtmlTextInput) page.getElementById("login.username_index_0");
		usernameField.setText(USERNAME);

		HtmlPasswordInput passwordField = (HtmlPasswordInput) page.getElementById("login.password_index_0");
		passwordField.setText(PASSWORD);

		HtmlImageInput loginButton = (HtmlImageInput) page.getElementById("Submit");
		loginButton.click();

		webClient.getOptions().setJavaScriptEnabled(javaScriptEnabled);
	}
}
