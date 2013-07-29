package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fingy.micromedex.web.dto.SearchResults;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

@Controller
public class MethodOneController extends AbstractMicromedexController {

	@ResponseBody
	@RequestMapping("/drugs")
	public SearchResults<String> drugs(@RequestParam(value = "name", required = true) final String name) throws FailingHttpStatusCodeException,
			MalformedURLException, IOException {
		String[][] searchResults = getDrugsWithNameLike(name);
		return new SearchResults<String>(extractOnlyNameFromResults(searchResults));
	}

	private List<String> extractOnlyNameFromResults(final String[][] searchResults) {
		List<String> resultData = new ArrayList<>();
		for (int i = RESULTS_START_INDEX; i < searchResults.length; i++) {
			resultData.add(searchResults[i][NAME_INDEX]);
		}
		return resultData;
	}

	@ResponseBody
	@RequestMapping("/allergies")
	public SearchResults<String> allergies(@RequestParam(value = "name", required = true) final String name) throws FailingHttpStatusCodeException,
			MalformedURLException, IOException {
		String[][] searchResults = getAllergiesWithNameLike(name);
		return new SearchResults<String>(extractOnlyNameFromResults(searchResults));
	}
}
