package com.fingy.aprod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.aprod.scrape.AdPageContactJsoupScraper;
import com.fingy.aprod.scrape.ContactJsoupScraper;
import com.fingy.aprod.scrape.FirstAdPageJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.jsoup.HttpClientParserUtil;
import com.fingy.scrape.queue.ScraperLinksQueue;
import com.fingy.scrape.security.util.TorUtil;

public class AprodScraperScheduler {

	private static final int CATEGORY_TIMEOUT = 20000;
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private static boolean shouldUseTor = false;

	private ExecutorService categoryScrapingThreadPool;
	private ExecutorService contactScrapingThreadPool;
	private ExecutorCompletionService<Contact> itemScrapingCompletionService;

	private ScraperLinksQueue linksQueue;
	private Set<String> queuedLinks;
	private Set<Contact> scrapedItems;
	private String outputExcel;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			ExecutionException {
		scrapeWhileThereAreResults();
		// new AprodScraperScheduler(getOutputFileForIteration()).doScrape();
	}

	private static void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {

		int count = 0;
		do {
			if (shouldUseTor) {
				TorUtil.stopTor();
				TorUtil.startAndUseTorAsProxy();
				Thread.sleep(30000);
			} else {
				TorUtil.disableSocksProxy();
			}

			count = new AprodScraperScheduler(getOutputFileForIteration()).doScrape();

			if (shouldUseTor) {
				TorUtil.stopTor();
			}

			Thread.sleep(60000);
		} while (count > 0);
	}

	private static String getOutputFileForIteration() {
		return "aprod-" + System.currentTimeMillis() + ".xlsx";
	}

	public AprodScraperScheduler(final String outputExcelFile) {
		categoryScrapingThreadPool = createDefaultThreadPool();
		contactScrapingThreadPool = Executors.newSingleThreadExecutor();
		itemScrapingCompletionService = new ExecutorCompletionService<Contact>(contactScrapingThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedLinks = new LinkedHashSet<String>();
		scrapedItems = new LinkedHashSet<Contact>();

		outputExcel = outputExcelFile;
	}

	public int doScrape() {
		int queuedSize = 0;
		try {
			HttpClientParserUtil.resetClient();

			loadVisitedLinksFromFile();
			loadQueuedLinksFromFile();

			new FirstAdPageJsoupScraper("http://aprod.hu/budapest/", linksQueue).call();
			new FirstAdPageJsoupScraper("http://aprod.hu/budapest/?search[offer_seek]=seek", linksQueue).call();

			AdultItemJsoupScraper.setSessionExpired(false);
			createSession();
			submitAdPageScrapingTasksWhileThereIsEnoughWork();

			ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(categoryScrapingThreadPool, 10, TimeUnit.MINUTES);
			ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(contactScrapingThreadPool, 10, TimeUnit.MINUTES);

			saveResultsToExcelAndDownloadImages();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			saveVisitedLinksToFile();
			queuedSize = saveQueuedLinksToFile();
		}

		System.out.println("doScrape() - " + scrapedItems.size());
		return queuedSize;
	}

	private void createSession() throws IOException {
		// cookiees = Jsoup.connect("http://aprod.hu/budapest/").timeout(0).execute().cookies();
	}

	private void loadVisitedLinksFromFile() {
		try {
			final Set<String> visited = new HashSet<String>(FileUtils.readLines((new File("visited.txt"))));
			linksQueue.markAllVisited(visited);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadQueuedLinksFromFile() {
		try {
			final Set<String> queued = new HashSet<String>(FileUtils.readLines((new File("queued.txt"))));
			linksQueue.addAllIfNotVisited(queued);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void submitAdPageScrapingTasksWhileThereIsEnoughWork() {
		System.out.println("submitScrapingTasksWhileThereIsEnoughWork()");
		while (stillHaveLinksToBeScraped()) {
			if (AbstractAdultItemJsoupScraper.isSessionExpired()) {
				System.out.println("Session expired, breaking");
				break;
			}

			try {
				String link = linksQueue.take();

				if (isAdPage(link)) {
					submitAdPageScrapingTask(link);
				} else {
					queuedLinks.add(link);
					submitContactScrapingTask(link);
				}

				// long sleepInterval = 500 + new Random(System.currentTimeMillis()).nextInt(1500);
				// Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private boolean isAdPage(String link) {
		return link.contains("aprod.hu/budapest/");
	}

	private boolean stillHaveLinksToBeScraped() {
		return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
	}

	private void submitAdPageScrapingTask(final String link) {
		try {
			categoryScrapingThreadPool.submit(new AdPageContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void submitContactScrapingTask(final String link) {
		try {
			itemScrapingCompletionService.submit(new ContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveResultsToExcelAndDownloadImages() throws FileNotFoundException, IOException {
		while (true) {
			try {
				final Future<Contact> future = itemScrapingCompletionService.poll(20, TimeUnit.SECONDS);
				Contact contact = future.get();

				if (contact.isValid())
					scrapedItems.add(contact);
			} catch (Exception e) {
				break;
			}
		}

		// createExcelSheetFromScrapedItems(scrapedItems);
		FileUtils.writeLines(new File("contacts.txt"), scrapedItems, true);
	}

	public void createExcelSheetFromScrapedItems(final Collection<Contact> items) throws FileNotFoundException,
			IOException {
		System.out.println("createExcelSheetFromScrapedItems()");
		new ContactToExcelBuilder().buildExcel(items).writeToFile(outputExcel);
	}

	private void saveVisitedLinksToFile() {
		try {
			System.out.println("saveVisitedLinksToFile()");
			FileUtils.writeLines(new File("visited.txt"), linksQueue.getVisitedLinks());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int saveQueuedLinksToFile() {
		try {
			System.out.println("saveQueuedLinksToFile()");

			Set<String> temp = new HashSet<String>();
			temp.addAll(linksQueue.getQueuedLinks());
			temp.addAll(queuedLinks);
			temp.removeAll(linksQueue.getVisitedLinks());

			FileUtils.writeLines(new File("queued.txt"), temp);

			return temp.size();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	private ExecutorService createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 5, Integer.MAX_VALUE, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>());
	}

	// private boolean isAdDescriptionPage(String href) {
	// return href.contains("hirdetes");
	// }
}
