package com.fingy.yellowpages.scrape;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.yellowpages.CompanyDetails;

public class CompanyDetailsScraperTest {

	private static final String PAGE_LOCATION = "company-details.htm";

	private CompanyDetailsScraper scraper = new CompanyDetailsScraper("");

	@Test
	public void testScrapePage() throws Exception {
		String pageToScrape = FileUtils.readFileToString(getPageFile());
		Document page = Jsoup.parse(pageToScrape);

		CompanyDetails expected = new CompanyDetails("North American Dismantling & Demolition", "Serving Your Area.", "(800) 664-3697", "http://www.nadc1.com",
				"info@nadc1.com");
		Assertions.assertThat(scraper.scrapePage(page)).isEqualTo(expected);
	}

	private File getPageFile() {
		String filePath = getClass().getClassLoader().getResource(PAGE_LOCATION).getFile();
		return new File(filePath);
	}

}
