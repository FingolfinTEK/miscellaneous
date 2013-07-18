package com.fingy.ehentai.scrape;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.fingy.ehentai.MangaInfo;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class MangaInfoScraperTest {
	private static final String MANGA_PAGE_HTM_LOCATION = "test_pages/manga-page.htm";

	private static final String TITLE = "(C82) [Seniman Kartun (Kosuke Haruhito)] Joshikou Saber (Fate/Zero)english version 1 - E-Hentai Galleries";
	private static final String IMAGES = "27 @ 13.55 MB";
	private static final String TAGS = "parody: fate zero (206)\n" + "character: saber (234) kiritsugu emiya (89)\n"
			+ "group: seniman kartun (198)\n" + "artist: kosuke haruhito (170)\n" + "male: schoolboy (42)\n"
			+ "female: stockings (132) schoolgirl (96) ahegao (42)\n";
	private static final String COVER_IMAGE_URL = "http://94.242.229.81:8960/h/6da4dd689460ba656b52b4dd514be3dc55304a8f-370335-1059-1500-jpg/keystamp=1374167369-bea118b33d/_IMG_0001.jpg";

	private static final MangaInfo MANGA_INFO = new MangaInfo(TITLE, "", IMAGES, TAGS, COVER_IMAGE_URL);

	private ScraperLinksQueue linksQueue = new ScraperLinksQueue();
	private MangaInfoScraper mangaScraper = new MangaInfoScraper("", linksQueue);

	@Test
	public void testScrapePage() throws Exception {
		String mainPage = FileUtils.readFileToString(getMainPageFile());
		Document page = Jsoup.parse(mainPage);

		assertThat(linksQueue.getVisitedLinks()).isEmpty();
		assertThat(mangaScraper.scrapePage(page)).isEqualTo(MANGA_INFO);
		assertThat(linksQueue.getVisitedLinks()).contains(mangaScraper.getScrapeUrl());
	}

	private File getMainPageFile() {
		String filePath = getClass().getClassLoader().getResource(MANGA_PAGE_HTM_LOCATION).getFile();
		return new File(filePath);
	}

}
