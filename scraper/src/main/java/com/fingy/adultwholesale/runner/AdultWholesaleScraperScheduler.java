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

import net.sf.jasperreports.engine.JRException;

import com.fingy.adultwholesale.AdultItem;
import com.fingy.adultwholesale.AdultItemToExcelBuilder;
import com.fingy.adultwholesale.scrape.AdultItemCategoryJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultWholesaleScraperScheduler {

	public static void main(String[] args) throws JRException, FileNotFoundException, IOException,
			InterruptedException, ExecutionException {
		LinkedBlockingQueue<Future<AdultItem>> futureLinks = new LinkedBlockingQueue<Future<AdultItem>>();
		CompletionService<AdultItem> completionService = new ExecutorCompletionService<AdultItem>(
				Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5), futureLinks);
		ExecutorService service = Executors.newCachedThreadPool();
		ScraperLinksQueue linksQueue = new ScraperLinksQueue();
		Future<Object> future = service.submit(new AdultItemCategoryJsoupScraper(
				"http://adultwholesaledirect.com/tour/index.php?main_page=page&id=28", linksQueue));

		future.get();

		while (!linksQueue.isEmpty()) {
			final String link = linksQueue.take();

			if (isCategoryLink(link))
				service.submit(new AdultItemCategoryJsoupScraper(link, linksQueue));
			else
				completionService.submit(new AdultItemJsoupScraper(link));
		}

		final AdultItem item = new AdultItemJsoupScraper(
				"http://adultwholesaledirect.com/tour/index.php?main_page=product_info&cPath=127_144&products_id=5268")
				.call();

		System.out.println(item);

		final List<AdultItem> items = new ArrayList<AdultItem>();
		for (Future<AdultItem> futureLink : futureLinks)
			items.add(futureLink.get());

		new AdultItemToExcelBuilder(items).buildExcel().write(
			new FileOutputStream("C:/Users/Fingy/Desktop/wholesale.xlsx"));
	}

	private static boolean isCategoryLink(String href) {
		return href.contains("cPath");
	}
}
