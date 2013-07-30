package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.micromedex.util.HtmlUnitUtil;
import com.fingy.micromedex.util.JsonUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

public class AbstractMicromedexController {

	protected static final int NAME_INDEX = 0;
	protected static final int RESULTS_START_INDEX = 1;

	protected static final String DRUG_QUERY_URL_FORMAT = "http://www.micromedexsolutions.com/micromedex2/librarian/"
			+ "PFDefaultActionId/evidencexpert.AjaxSearch?" + "searchType=diName&channel=drug&SearchTerm=%%5E%s&randomNum=%d&LoadLimit=0";
	protected static final String ALLERGY_QUERY_URL_FORMAT = "http://www.micromedexsolutions.com/micromedex2/librarian/"
			+ "PFDefaultActionId/evidencexpert.AjaxSearch?" + "searchType=diAllergy&channel=drug&SearchTerm=%%5E%s&randomNum=%d&LoadLimit=0";

	private final ThreadLocal<WebClient> webClientHolder = new WebClientHoldingThreadLocal();

	public ThreadLocal<WebClient> getWebClientHolder() {
		return webClientHolder;
	}

	protected String[][] getDrugsWithNameLike(final String name) throws IOException, MalformedURLException, JsonParseException, JsonMappingException {
		return getFromUrlQueryForName(DRUG_QUERY_URL_FORMAT, name);
	}

	protected String[][] getFromUrlQueryForName(String queryUrlFormat, final String name) throws IOException, MalformedURLException, JsonParseException,
			JsonMappingException {
		WebClient webClient = getWebClientHolder().get();
		TextPage response = webClient.getPage(String.format(queryUrlFormat, name, getRandomInteger()));
		webClient.closeAllWindows();
		return parseJson(response);
	}

	private int getRandomInteger() {
		return new Random().nextInt(10000);
	}

	private String[][] parseJson(final TextPage data) throws IOException, JsonParseException, JsonMappingException {
		String unescapedContent = getHtmlUnescapedContent(data);
		Class<String[][]> valueType = String[][].class;
		return JsonUtil.readStringValueAsType(unescapedContent, valueType);
	}

	private String getHtmlUnescapedContent(final TextPage data) {
		String escapedJsonContent = data.getContent();
		return StringEscapeUtils.unescapeHtml4(escapedJsonContent);
	}

	protected String[][] getAllergiesWithNameLike(final String name) throws IOException, MalformedURLException, JsonParseException, JsonMappingException {
		return getFromUrlQueryForName(ALLERGY_QUERY_URL_FORMAT, name);
	}

	public final class WebClientHoldingThreadLocal extends ThreadLocal<WebClient> {
		private final Logger logger = LoggerFactory.getLogger(getClass());

		@Override
		protected WebClient initialValue() {
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
			try {
				HtmlUnitUtil.doLogin(webClient);
				webClient.closeAllWindows();
			} catch (FailingHttpStatusCodeException | IOException e) {
				logger.error("Error during logging", e);
			}
			return webClient;
		}
	}
}
