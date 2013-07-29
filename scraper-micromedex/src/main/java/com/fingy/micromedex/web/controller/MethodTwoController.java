package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@Controller
public class MethodTwoController extends AbstractMicromedexController {

	private static final String INTERACTIONS_QUERY = "http://www.micromedexsolutions.com/micromedex2/librarian/ND_T/evidencexpert/ND_PR/"
			+ "evidencexpert/CS/5C9FDD/ND_AppProduct/evidencexpert/DUPLICATIONSHIELDSYNC/53CD75/ND_PG/evidencexpert/ND_B/evidencexpert/ND_P/"
			+ "evidencexpert/PFActionId/evidencexpert.FindDrugInteractions";

	@ResponseBody
	@RequestMapping("/interactions")
	public Object interactions(@RequestParam(value = "drugs", required = true) final String drugsToSearch,
			@RequestParam(value = "allergies", required = true) final String allergiesToSearch) throws IOException {

		WebClient webClient = getWebClientHolder().get();

		HtmlPage page = webClient.getPage(INTERACTIONS_QUERY);
		ObjectMapper mapper = new ObjectMapper();

		HtmlHiddenInput drugsInput = (HtmlHiddenInput) page.getElementById("selectedDrugs");
		List<String[]> drugs = getDrugsFromMicromedex(drugsToSearch);
		drugsInput.setValueAttribute(mapper.writeValueAsString(drugs));

		HtmlHiddenInput allergiesInput = (HtmlHiddenInput) page.getElementById("selectedAllergies_index_0");
		List<String[]> allergies = getAllergiesFromMicromedex(allergiesToSearch);
		allergiesInput.setValueAttribute(mapper.writeValueAsString(allergies));

		HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getByXPath("/html/body/div[7]/div/form/div/div[5]/table/tbody/tr/td[2]/div[2]/div/div[2]/div[4]/input[2]").get(0);
		submitButton.setAttribute("disabled", "");

		HtmlPage resultsPage = submitButton.click();
		return resultsPage.asXml();
	}

	private List<String[]> getDrugsFromMicromedex(final String drugsToSearch) throws IOException, MalformedURLException, JsonParseException,
			JsonMappingException {
		List<String[]> drugs = new ArrayList<>();

		StringTokenizer drugTokenizer = new StringTokenizer(drugsToSearch, ",");
		while (drugTokenizer.hasMoreTokens()) {
			String drugName = drugTokenizer.nextToken();
			String[][] foundDrugs = getDrugsWithNameLike(drugName);
			for (String[] drug : foundDrugs) {
				if (drug[NAME_INDEX].equalsIgnoreCase(drugName)) {
					drugs.add(drug);
				}
			}
		}

		return drugs;
	}

	private List<String[]> getAllergiesFromMicromedex(final String drugsToSearch) throws IOException, MalformedURLException, JsonParseException,
			JsonMappingException {
		List<String[]> allergies = new ArrayList<>();

		StringTokenizer allergyTokenizer = new StringTokenizer(drugsToSearch, ",");
		while (allergyTokenizer.hasMoreTokens()) {
			String drugName = allergyTokenizer.nextToken();
			String[][] foundAllergies = getAllergiesWithNameLike(drugName);
			for (String[] allergy : foundAllergies) {
				if (allergy[NAME_INDEX].equalsIgnoreCase(drugName)) {
					allergies.add(allergy);
				}
			}
		}

		return allergies;
	}
}
