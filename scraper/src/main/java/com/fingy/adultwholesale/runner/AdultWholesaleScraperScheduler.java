package com.fingy.adultwholesale.runner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.adultwholesale.AdultItemToExcelBuilder;
import com.fingy.adultwholesale.scrape.AdultItemCategoryJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultWholesaleScraperScheduler {

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final String STARTING_URL = "http://adultwholesaledirect.com/tour/index.php?main_page=page&id=28";
	private static final int THRESHOLD = 50;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		// System.getProperties().put("proxySet", "true");
		// System.getProperties().put("socksProxyHost", "10.0.103.107");
		// System.getProperties().put("socksProxyPort", "9090");

		final ExecutorService threadPool = Executors.newCachedThreadPool(); // createDefaultThreadPool();
		final CompletionService<AdultItem> itemScrapingCompletionService = new ExecutorCompletionService<AdultItem>(threadPool);
		final ScraperLinksQueue linksQueue = new ScraperLinksQueue();

		threadPool.submit(new AdultItemCategoryJsoupScraper(STARTING_URL, linksQueue)).get();

		int scrapedProductsCount = 0;
		while (!linksQueue.isEmpty()) {
			final String link = linksQueue.take();

			if (isItemDescriptionPage(link)) {
				itemScrapingCompletionService.submit(new AdultItemJsoupScraper(link, linksQueue));
				scrapedProductsCount++;
			} else
				threadPool.submit(new AdultItemCategoryJsoupScraper(link, linksQueue));

			if (scrapedProductsCount == THRESHOLD) {
				threadPool.shutdown();
			}

			Thread.sleep(100);
		}

		System.out.println("Awaiting termination of the services");
		threadPool.awaitTermination(1, TimeUnit.MINUTES);

		if (scrapedProductsCount > 0) {
			final List<AdultItem> items = new ArrayList<AdultItem>();

			for (int i = 0; i < scrapedProductsCount; i++) {
				try {
					Future<AdultItem> future = itemScrapingCompletionService.poll();
					items.add(future.get());
				} catch (Exception e) {
				}
			}

			new AdultItemToExcelBuilder().buildExcel(items).writeToFile("C:/Users/milos.milivojevic/Desktop/wholesale.xlsx");
		}
	}

	private static ThreadPoolExecutor createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 4, AVAILABLE_PROCESSORS * 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	private static boolean isItemDescriptionPage(String href) {
		return href.contains("products_id");
	}
}
