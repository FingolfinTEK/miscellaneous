package com.fingy.ehentai.scrape;


import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.scrape.queue.ScraperLinksQueue;

public class SearchPageMangaLinkScraperTest {

	private static final String MAIN_PAGE_HTM_LOCATION = "test_pages/main-page.htm";

	private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
	private SearchPageMangaLinksScraper mangaLinksScraper = new SearchPageMangaLinksScraper("", linksQueue);

	@Test
	public void testScrapePage() throws Exception {
		String mainPage = FileUtils.readFileToString(getMainPageFile());
		Document page = Jsoup.parse(mainPage);

        assertThat(linksQueue.getVisitedLinks()).isEmpty();
		assertThat(mangaLinksScraper.scrapePage(page)).isEqualTo(25);
		assertThat(linksQueue.getSize()).isEqualTo(25);
        assertThat(linksQueue.getVisitedLinks()).contains("");
	}

	private File getMainPageFile() {
		String filePath = getClass().getClassLoader().getResource(MAIN_PAGE_HTM_LOCATION).getFile();
		return new File(filePath);
	}

}
