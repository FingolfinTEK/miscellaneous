package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fingy.micromedex.util.HtmlUnitUtil;
import com.fingy.micromedex.web.dto.DrugResult;
import com.fingy.micromedex.web.dto.SearchResults;
import com.fingy.scrape.util.JsoupParserUtil;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@Controller
public class MethodThreeController extends AbstractMicromedexController {

	private static final String DISEASE_CHECK_URL_FORMAT = "http://www.micromedexsolutions.com/micromedex2/librarian/ND_T/evidencexpert/ND_PR/"
			+ "evidencexpert/CS/9535B4/ND_AppProduct/evidencexpert/DUPLICATIONSHIELDSYNC/E555E0/ND_PG/evidencexpert/ND_B/evidencexpert/ND_P/evidencexpert/"
			+ "PFActionId/evidencexpert.DoIntegratedSearch?SearchTerm=drugs%20that%20cause%20";

	@ResponseBody
	@RequestMapping("/check-disease")
	@Cacheable(value = "methodThreeCache", key = "#name")
	public SearchResults<DrugResult> checkDisease(@RequestParam(value = "name", required = true) final String name)
			throws FailingHttpStatusCodeException, IOException {
		Document parsedPage = getParsedPage(DISEASE_CHECK_URL_FORMAT + name);
		return extractDrugResultsFromPage(parsedPage);
	}

	private Document getParsedPage(String parseUrl) throws IOException, MalformedURLException {
		WebClient webClient = getWebClientHolder().get();
		return JsoupParserUtil.getPageFromUrlWithCookies(parseUrl, HtmlUnitUtil.getCookiesAsMap(webClient));
	}

	private SearchResults<DrugResult> extractDrugResultsFromPage(Document parsedPage) {
		List<DrugResult> results = new ArrayList<>();

		Elements links = parsedPage.select("#alphaBrowseResultContent:first-child div.titleLink a");
		for (Element link : links) {
			results.add(new DrugResult(link.text(), link.attr("href")));
		}

		return new SearchResults<>(results);
	}

}
