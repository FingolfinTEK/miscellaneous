package com.fingy.adultwholesale;

import static com.fingy.scrape.jsoup.AbstractJsoupScraper.USER_AGENT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fingy.scrape.AbstractScraper;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.fingy.adultwholesale.scrape.AdultItemCategoryJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultWholesaleScraperScheduler {

	private static final int CATEGORY_TIMEOUT = 10000;
	private static final String WHOLESALE_QUEUED_TXT = "wholesale-queued.txt";
	private static final String WHOLESALE_VISITED_TXT = "wholesale-visited.txt";
	private static final String DEFAULT_OUTPUT_EXCEL = "wholesale";

	private static final String[] LOGIN_CREDENTIALS = new String[] { "username", "47198", "pass", "hithlum" };
	private static final String LOGIN_PAGE = "http://www.adultwholesaledirect.com/login.php";
	private static final String STARTING_URL = "http://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkcategories.php";

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private Map<String, String> cookies;

	private ExecutorService categoryScrapingThreadPool;
	private ExecutorCompletionService<AdultItem> categoryScrapingCompletionService;
	private ExecutorService itemScrapintThreadPool;
	private ExecutorCompletionService<AdultItem> itemScrapingCompletionService;

	private ScraperLinksQueue linksQueue;
	private Set<String> queuedCategories;
	private Set<String> queuedProducts;
	private Set<String> scrapedCategories;
	private Set<AdultItem> scrapedItems;

	private String outputExcel;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		for (int i = 0; i < 100; i++) {
			scrapeWhileThereAreResults();
		}
	}

	private static void scrapeWhileThereAreResults() throws ExecutionException, IOException {
		int count = 0;
		do {
			count = new AdultWholesaleScraperScheduler(getOutputFileForIteration()).doScrape();
		} while (count > 0);
	}

	private static String getOutputFileForIteration() {
		return DEFAULT_OUTPUT_EXCEL + System.currentTimeMillis() + ".xlsx";
	}

	public AdultWholesaleScraperScheduler(final String outputExcelFile) {
		categoryScrapingThreadPool = createDefaultThreadPool();
		itemScrapintThreadPool = createDefaultThreadPool();
		categoryScrapingCompletionService = new ExecutorCompletionService<AdultItem>(categoryScrapingThreadPool);
		itemScrapingCompletionService = new ExecutorCompletionService<AdultItem>(itemScrapintThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedProducts = new HashSet<String>();
		queuedCategories = new LinkedHashSet<String>();
		scrapedCategories = new LinkedHashSet<String>();
		scrapedItems = new LinkedHashSet<AdultItem>();

		outputExcel = outputExcelFile;
	}

	public int doScrape() throws ExecutionException, IOException {
		System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
		System.getProperties().setProperty("socksProxyPort", "9150");

		AbstractScraper.setScrapeCompromised(false);
		doLogin();

		loadVisitedLinksFromFile();
		loadQueuedLinksFromFile();

		processEntryPageIfNeeded();
		submitScrapingTasksWhileThereIsEnoughWork();
		
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(categoryScrapingThreadPool, 10, TimeUnit.SECONDS);
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(itemScrapintThreadPool, 10, TimeUnit.SECONDS);

		saveResultsToExcelAndDownloadImages();
		saveVisitedLinksToFile();
		saveQueuedLinksToFile();

		System.out.println("AdultWholesaleScraperScheduler.doScrape() - " + scrapedItems.size());
		return scrapedItems.size();
	}

	private void loadVisitedLinksFromFile() {
		try {
			final Set<String> visited = new HashSet<String>(FileUtils.readLines((new File(WHOLESALE_VISITED_TXT))));
			linksQueue.markAllVisited(visited);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadQueuedLinksFromFile() {
		try {
			final Set<String> queued = new HashSet<String>(FileUtils.readLines((new File(WHOLESALE_QUEUED_TXT))));
			linksQueue.addAllIfNotVisited(queued);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doLogin() throws IOException {
		final Response response = Jsoup.connect(LOGIN_PAGE).data(LOGIN_CREDENTIALS).method(Method.POST).userAgent(USER_AGENT).timeout(0).execute();
		cookies = response.cookies();
	}

	private void processEntryPageIfNeeded() throws ExecutionException {
		System.out.println("AdultWholesaleScraperScheduler.processEntryPage()");
		try {
			Callable<AdultItem> entryPageTask = new AdultItemCategoryJsoupScraper(cookies, STARTING_URL, linksQueue);
			categoryScrapingCompletionService.submit(entryPageTask).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void submitScrapingTasksWhileThereIsEnoughWork() {
		System.out.println("AdultWholesaleScraperScheduler.submitScrapingTasksWhileThereIsEnoughWork()");
		while (stillHaveLinksToBeScraped()) {
			if (AbstractScraper.isScrapeCompromised()) {
				System.out.println("Session expired, breaking");
				break;
			}
			try {
				String link = linksQueue.take();

				if (isItemDescriptionPage(link)) {
					submitItemScrapingTask(link);
					queuedProducts.add(link);
				} else {
					submitCategoryScrapingTask(link);
					queuedCategories.add(link);
				}
			} catch (InterruptedException e) {
				break;
			}
		}

		System.out.println("submitScrapingTasksWhileThereIsEnoughWork() - discovered " + queuedCategories.size() + " categories");
		System.out.println("submitScrapingTasksWhileThereIsEnoughWork() - discovered " + queuedProducts.size() + " new items");
	}

	private boolean stillHaveLinksToBeScraped() {
		return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
	}

	private void submitCategoryScrapingTask(final String link) {
		try {
			categoryScrapingCompletionService.submit(new AdultItemCategoryJsoupScraper(cookies, link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void submitItemScrapingTask(final String link) {
		try {
			itemScrapingCompletionService.submit(new AdultItemJsoupScraper(cookies, link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveResultsToExcelAndDownloadImages() throws FileNotFoundException, IOException {
		System.out.println("AdultWholesaleScraperScheduler.saveResultsToExcelAndDownloadImages()");

		for (int i = 0; i < queuedCategories.size(); i++) {
			try {
				final Future<AdultItem> future = categoryScrapingCompletionService.poll(20, TimeUnit.SECONDS);
				scrapedCategories.add(future.get().getProductUrl());
			} catch (Exception e) {
				break;
			}
		}

		for (int i = 0; i < queuedProducts.size(); i++) {
			try {
				final Future<AdultItem> future = itemScrapingCompletionService.poll(20, TimeUnit.SECONDS);
				scrapedItems.add(future.get());
			} catch (Exception e) {
				break;
			}
		}

		createExcelSheetFromScrapedItems(scrapedItems);
	}

	private void saveVisitedLinksToFile() throws IOException {
		System.out.println("AdultWholesaleScraperScheduler.saveVisitedLinksToFile()");

		Set<String> visited = new HashSet<String>(scrapedCategories);

		for (AdultItem item : scrapedItems) {
			visited.add(item.getProductUrl());
		}

		FileUtils.writeLines(new File(WHOLESALE_VISITED_TXT), visited, true);
	}

	private void saveQueuedLinksToFile() throws IOException {
		System.out.println("AdultWholesaleScraperScheduler.saveQueuedLinksToFile()");

		queuedCategories.removeAll(scrapedCategories);

		for (AdultItem item : scrapedItems) {
			queuedProducts.remove(item.getProductUrl());
		}

		FileUtils.writeLines(new File(WHOLESALE_QUEUED_TXT), queuedProducts);
		FileUtils.writeLines(new File(WHOLESALE_QUEUED_TXT), queuedCategories, true);
	}

	private void createExcelSheetFromScrapedItems(final Collection<AdultItem> items) throws FileNotFoundException, IOException {
		System.out.println("AdultWholesaleScraperScheduler.createExcelSheetFromScrapedItems()");
		new AdultItemToExcelBuilder().buildExcel(items).writeToFile(outputExcel);
	}

	private ExecutorService createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 10, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	private boolean isItemDescriptionPage(String href) {
		return href.contains("ajax_getbulkproductdetails");
	}
}
