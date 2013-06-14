package com.fingy.adultwholesale.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.adultwholesale.AdultItemToExcelBuilder;
import com.fingy.adultwholesale.scrape.AdultItemCategoryJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultWholesaleScraperScheduler {

	private static final String LOGIN_PAGE = "https://www.adultwholesaledirect.com/login.php";
	private static final String STARTING_URL = "https://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkcategories.php";
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final int THRESHOLD = 50;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		System.getProperties().setProperty("https.proxyHost", "");
		System.getProperties().setProperty("https.proxyPort", "");

		final Response response = Jsoup.connect(LOGIN_PAGE).data("username", "47198", "pass", "hithlum").method(Method.POST)
				.userAgent(AbstractJsoupScraper.USER_AGENT).execute();
		final Map<String, String> cookies = response.cookies();

		final ExecutorService categoryScrapingThreadPool = createDefaultThreadPool();
		final ExecutorService itemScrapintThreadPool = Executors.newCachedThreadPool();
		final CompletionService<AdultItem> itemScrapingCompletionService = new ExecutorCompletionService<AdultItem>(itemScrapintThreadPool);
		final ScraperLinksQueue linksQueue = new ScraperLinksQueue();

		categoryScrapingThreadPool.submit(new AdultItemCategoryJsoupScraper(cookies, STARTING_URL, linksQueue)).get();

		int scrapedProductsCount = 0;
		while (!linksQueue.isEmpty()) {
			final String link = linksQueue.take();

			if (isItemDescriptionPage(link)) {
				try {
					itemScrapingCompletionService.submit(new AdultItemJsoupScraper(cookies, link, linksQueue));
				} catch (Exception e) {
					e.printStackTrace();
				}

				scrapedProductsCount++;
			} else {
				try {
					categoryScrapingThreadPool.submit(new AdultItemCategoryJsoupScraper(cookies, link, linksQueue));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (scrapedProductsCount == THRESHOLD) {
				categoryScrapingThreadPool.shutdownNow();
			}

			Thread.sleep(100);
		}

		System.out.println("Awaiting termination of the services");
		categoryScrapingThreadPool.awaitTermination(10, TimeUnit.SECONDS);
		itemScrapintThreadPool.shutdownNow();

		if (scrapedProductsCount > 0) {
			final List<AdultItem> items = new ArrayList<AdultItem>();

			for (int i = 0; i < scrapedProductsCount; i++) {
				try {
					Future<AdultItem> future = itemScrapingCompletionService.poll();
					items.add(future.get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			new AdultItemToExcelBuilder().buildExcel(items).writeToFile("C:/Users/milos.milivojevic/Desktop/wholesale.xlsx");
		}
	}

	private static ThreadPoolExecutor createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 4, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	private static boolean isItemDescriptionPage(String href) {
		return href.contains("ajax_getbulkproductdetails");
	}
}
