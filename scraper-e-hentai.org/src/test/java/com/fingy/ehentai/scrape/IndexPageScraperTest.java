package com.fingy.ehentai.scrape;


import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class IndexPageScraperTest {

	private static final String MAIN_PAGE_HTM_LOCATION = "test_pages/main-page.htm";

	private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
	private IndexPageScraper indexPageScraper = new IndexPageScraper("", linksQueue);

	@Test
	public void testScrapePage() throws Exception {
		String mainPage = FileUtils.readFileToString(getMainPageFile());
		Document page = Jsoup.parse(mainPage);

		assertThat(indexPageScraper.scrapePage(page)).isEqualTo(403);
		assertThat(linksQueue.getSize()).isEqualTo(403);
	}

	private File getMainPageFile() {
		String filePath = getClass().getClassLoader().getResource(MAIN_PAGE_HTM_LOCATION).getFile();
		return new File(filePath);
	}

}
