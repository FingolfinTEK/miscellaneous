package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@Controller
public class MethodOneMicromedexController {

    private static final int NAME_INDEX = 0;
    private static final int RESULTS_START_INDEX = 1;

    private static final String USERNAME = "TRI1";
    private static final String PASSWORD = "33tybs@h";

    private static final String START_URL = "http://www.micromedexsolutions.com/home/dispatch";
    private static final String DRUG_QUERY_URL_FORMAT = "http://www.micromedexsolutions.com/micromedex2/librarian/"
            + "PFDefaultActionId/evidencexpert.AjaxSearch?" + "searchType=diName&channel=drug&SearchTerm=%%5E%s&randomNum=%d&LoadLimit=0";
    private static final String ALLERGY_QUERY_URL_FORMAT = "http://www.micromedexsolutions.com/micromedex2/librarian/"
            + "PFDefaultActionId/evidencexpert.AjaxSearch?"
            + "searchType=diAllergy&channel=drug&SearchTerm=%%5E%s&randomNum=%d&LoadLimit=0";

    private final ThreadLocal<WebClient> webClientHolder = new WebClientHoldingThreadLocal();

    @ResponseBody
    @RequestMapping("/drugs")
    public Object drugs(@RequestParam(value = "name", required = true) final String name) throws FailingHttpStatusCodeException,
            MalformedURLException, IOException {
        WebClient webClient = webClientHolder.get();
        TextPage response = webClient.getPage(String.format(DRUG_QUERY_URL_FORMAT, name, getRandomInteger()));
        return parseJsonAndExtractOnlyNames(response);
    }

    private Object parseJsonAndExtractOnlyNames(final TextPage data) throws IOException, JsonParseException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        String unescapedContent = getHtmlUnescapedContent(data);
        String[][] searchResults = mapper.readValue(unescapedContent, String[][].class);

        List<String> resultData = extractOnlyNameFromResults(searchResults);
        return resultData;
    }

    private String getHtmlUnescapedContent(final TextPage data) {
        String escapedJsonContent = data.getContent();
        return StringEscapeUtils.unescapeHtml4(escapedJsonContent);
    }

    private List<String> extractOnlyNameFromResults(final String[][] searchResults) {
        List<String> resultData = new ArrayList<>();
        for (int i = RESULTS_START_INDEX; i < searchResults.length; i++) {
            resultData.add(searchResults[i][NAME_INDEX]);
        }
        return resultData;
    }

    private void doLogin(final WebClient webClient) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage(START_URL);

        HtmlTextInput usernameField = (HtmlTextInput) page.getElementById("login.username_index_0");
        usernameField.setText(USERNAME);

        HtmlPasswordInput passwordField = (HtmlPasswordInput) page.getElementById("login.password_index_0");
        passwordField.setText(PASSWORD);

        HtmlImageInput loginButton = (HtmlImageInput) page.getElementById("Submit");
        loginButton.click();
    }

    private int getRandomInteger() {
        return new Random().nextInt(10000);
    }

    @ResponseBody
    @RequestMapping("/allergies")
    public Object allergies(@RequestParam(value = "name", required = true) final String name) throws FailingHttpStatusCodeException,
            MalformedURLException, IOException {
        WebClient webClient = webClientHolder.get();
        TextPage response = webClient.getPage(String.format(ALLERGY_QUERY_URL_FORMAT, name, getRandomInteger()));
        return parseJsonAndExtractOnlyNames(response);
    }

    private final class WebClientHoldingThreadLocal extends ThreadLocal<WebClient> {
        @Override
        protected WebClient initialValue() {
            WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
            try {
                doLogin(webClient);
            } catch (FailingHttpStatusCodeException | IOException e) {
                e.printStackTrace();
            }
            return webClient;
        }
    }
}
