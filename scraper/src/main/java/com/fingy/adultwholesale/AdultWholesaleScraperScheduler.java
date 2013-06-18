package com.fingy.adultwholesale;

import static com.fingy.scrape.jsoup.AbstractJsoupScraper.USER_AGENT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.fingy.adultwholesale.scrape.AdultItemCategoryJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.JsoupImageDownloader;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AdultWholesaleScraperScheduler {

	private static final int CATEGORY_TIMEOUT = 300000;
	private static final String WHOLESALE_QUEUED_TXT = "wholesale-queued.txt";
	private static final String WHOLESALE_VISITED_TXT = "wholesale-visited.txt";
	private static final String DEFAULT_OUTPUT_EXCEL = "wholesale";

	private static final String[] LOGIN_CREDENTIALS = new String[] { "username", "47198", "pass", "hithlum" };
	private static final String LOGIN_PAGE = "https://www.adultwholesaledirect.com/login.php";
	private static final String STARTING_URL = "https://www.adultwholesaledirect.com/customer/bulk/ajax_getbulkcategories.php";

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private static Set<String> discoveredCategories = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private Map<String, String> cookies;

	private ExecutorService categoryScrapingThreadPool;
	private ExecutorService imageScrapingThreadPool;
	private ExecutorService itemScrapintThreadPool;
	private ExecutorCompletionService<AdultItem> itemScrapingCompletionService;

	private ScraperLinksQueue linksQueue;
	private Set<String> queuedProducts;
	private List<AdultItem> items;

	private AtomicInteger scrapedProductsCount;

	private String inputExcel;
	private String outputExcel;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		scrapeWhileThereAreResults();
		Thread.sleep(5000);
		scrapeWhileThereAreResults();
	}

	private static void scrapeWhileThereAreResults() throws ExecutionException, IOException {
		int count = 0;
		int iteration = 0;
		do {
			count = new AdultWholesaleScraperScheduler(getInputFileForIteration(iteration), getOutputFileForIteration(iteration)).doScrape();
			iteration++;
		} while (count > 0);
	}

	private static String getOutputFileForIteration(int iteration) {
		return DEFAULT_OUTPUT_EXCEL + (iteration + 1) % 2 + ".xlsx";
	}

	private static String getInputFileForIteration(int iteration) {
		return DEFAULT_OUTPUT_EXCEL + iteration % 2 + ".xlsx";
	}

	public AdultWholesaleScraperScheduler(final String inputExcelFile, final String outputExcelFile) {
		categoryScrapingThreadPool = createDefaultThreadPool();
		imageScrapingThreadPool = createThreadPoolForImageDownload();
		itemScrapintThreadPool = createDefaultThreadPool();
		itemScrapingCompletionService = new ExecutorCompletionService<>(itemScrapintThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedProducts = new HashSet<>();
		items = new ArrayList<>();

		scrapedProductsCount = new AtomicInteger();

		inputExcel = inputExcelFile;
		outputExcel = outputExcelFile;
	}

	public int doScrape() throws ExecutionException, IOException {
		System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
		System.getProperties().setProperty("socksProxyPort", "9150");

		loadVisitedLinksFromFile();
		loadQueuedLinksFromFile();
		loadCategories();

		doLogin();

		processEntryPage();
		submitScrapingTasksWhileThereIsEnoughWork();
		saveResultsToExcelAndDownloadImages();

		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(categoryScrapingThreadPool, 10, TimeUnit.MINUTES);
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(itemScrapintThreadPool, 40, TimeUnit.MINUTES);

		return items.size();
	}

	private void loadVisitedLinksFromFile() {
		try {
			final Set<String> visited = new HashSet<>(FileUtils.readLines((new File(WHOLESALE_VISITED_TXT))));
			linksQueue.markAllVisited(visited);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadQueuedLinksFromFile() {
		try {
			final Set<String> queued = new HashSet<>(FileUtils.readLines((new File(WHOLESALE_QUEUED_TXT))));
			linksQueue.addAllIfNotVisited(queued);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadCategories() {
		linksQueue.addAllIfNotVisited(discoveredCategories);
	}

	private void doLogin() throws IOException {
		final Response response = Jsoup.connect(LOGIN_PAGE).data(LOGIN_CREDENTIALS).method(Method.POST).userAgent(USER_AGENT).timeout(0).execute();
		cookies = response.cookies();
	}

	private void processEntryPage() throws ExecutionException {
		try {
			Callable<AdultItem> entryPageTask = new AdultItemCategoryJsoupScraper(cookies, STARTING_URL, linksQueue);
			categoryScrapingThreadPool.submit(entryPageTask).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void submitScrapingTasksWhileThereIsEnoughWork() {
		while (stillHaveLinksToBeScraped()) {
			String link;
			try {
				link = linksQueue.take();

				if (isItemDescriptionPage(link)) {
					submitItemScrapingTask(link);
					queuedProducts.add(link);
					scrapedProductsCount.incrementAndGet();
				} else {
					submitCategoryScrapingTask(link);
					discoveredCategories.add(link);
				}
			} catch (InterruptedException e) {
			}
		}

		System.out.println("submitScrapingTasksWhileThereIsEnoughWork() - discovered " + discoveredCategories.size() + " categories");
		System.out.println("submitScrapingTasksWhileThereIsEnoughWork() - discovered " + queuedProducts.size() + " new items");
	}

	private boolean stillHaveLinksToBeScraped() {
		return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
	}

	private void saveResultsToExcelAndDownloadImages() throws FileNotFoundException, IOException {
		if (scrapedProductsCount.get() > 0) {
			items.clear();
			for (int i = 0; i < scrapedProductsCount.get(); i++) {
				try {
					final Future<AdultItem> future = itemScrapingCompletionService.poll(20, TimeUnit.SECONDS);
					final AdultItem item = future.get();

					items.add(item);
					submitImageDownloadingTaskForItem(item);
				} catch (Exception e) {
				}
			}

			createExcelSheetFromScrapedItems(items);
			saveVisitedLinksToFile();
			saveQueuedLinksToFile();

			ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(imageScrapingThreadPool, 40, TimeUnit.MINUTES);
		}
	}

	private void saveVisitedLinksToFile() throws IOException {
		Set<String> visited = new HashSet<>();

		for (AdultItem item : items) {
			visited.add(item.getProductUrl());
		}

		FileUtils.writeLines(new File(WHOLESALE_VISITED_TXT), visited, true);
	}

	private void saveQueuedLinksToFile() throws IOException {
		for (AdultItem item : items) {
			queuedProducts.remove(item.getProductUrl());
		}

		FileUtils.writeLines(new File(WHOLESALE_QUEUED_TXT), queuedProducts);
	}

	private void submitImageDownloadingTaskForItem(final AdultItem item) {
		String fileName = "./wholesale/" + item.getId();
		imageScrapingThreadPool.execute(new JsoupImageDownloader(item.getImageUrl(), fileName, cookies));
	}

	private void createExcelSheetFromScrapedItems(final List<AdultItem> items) throws FileNotFoundException, IOException {
		new AdultItemToExcelBuilder().openExcel(inputExcel).appendToExcel(items).writeToFile(outputExcel);
	}

	private void submitCategoryScrapingTask(final String link) {
		try {
			categoryScrapingThreadPool.submit(new AdultItemCategoryJsoupScraper(cookies, link, linksQueue));
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

	private ExecutorService createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 10, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	private ExecutorService createThreadPoolForImageDownload() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 40, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	private boolean isItemDescriptionPage(String href) {
		return href.contains("ajax_getbulkproductdetails");
	}
}
