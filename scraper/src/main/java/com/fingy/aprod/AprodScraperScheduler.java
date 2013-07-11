package com.fingy.aprod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

public class AprodScraperScheduler {

	private static final int CATEGORY_TIMEOUT = 20000;
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private ExecutorService adPageScrapingThreadPool;
	private ExecutorService contactScrapingThreadPool;
	private ExecutorCompletionService<Contact> contactScrapingCompletionService;

	private ScraperLinksQueue linksQueue;
	private Set<String> queuedLinks;
	private Set<Contact> scrapedItems;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			ExecutionException {
		// scrapeWhileThereAreResults();
		int count = new AprodScraperScheduler(getOutputFileForIteration()).doScrape();
		HttpClientParserUtil.resetClient();
		System.exit(count);
	}

	private static String getOutputFileForIteration() {
		return "aprod-" + System.currentTimeMillis() + ".xlsx";
	}

	public AprodScraperScheduler(final String outputExcelFile) {
		adPageScrapingThreadPool = createDefaultThreadPool();
		contactScrapingThreadPool = Executors.newSingleThreadExecutor();
		contactScrapingCompletionService = new ExecutorCompletionService<>(contactScrapingThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedLinks = new LinkedHashSet<>();
		scrapedItems = new LinkedHashSet<>();
	}

	public int doScrape() {
		int queuedSize = 0;
		try {
			loadContactsFromFile();
			loadVisitedLinksFromFile();
			loadQueuedLinksFromFile();

			new FirstAdPageJsoupScraper("http://aprod.hu/budapest/", linksQueue).call();
			new FirstAdPageJsoupScraper("http://aprod.hu/budapest/?search[offer_seek]=seek", linksQueue).call();

			AdultItemJsoupScraper.setSessionExpired(false);
			createSession();
			submitScrapingTasksWhileThereIsEnoughWork();

			ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(adPageScrapingThreadPool, 10, TimeUnit.MINUTES);
			ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(contactScrapingThreadPool, 10, TimeUnit.MINUTES);

			saveResults();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			saveVisitedLinksToFile();
			queuedSize = saveQueuedLinksToFile();
		}

		System.out.println("Scraped items: " + scrapedItems.size());
		return queuedSize;
	}

	private void loadContactsFromFile() {
		try {
			final List<String> lines = FileUtils.readLines((new File("contacts.txt")));
			for (String line : lines) {
				scrapedItems.add(Contact.fromString(line));
			}
			System.out.println("Loaded " + lines.size() + " contacts");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createSession() throws IOException {
		// cookiees = Jsoup.connect("http://aprod.hu/budapest/").timeout(0).execute().cookies();
	}

	private void loadVisitedLinksFromFile() {
		try {
			final Set<String> visited = new HashSet<String>(FileUtils.readLines((new File("visited.txt"))));
			linksQueue.markAllVisited(visited);
			System.out.println("Found " + visited.size() + " visited links");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadQueuedLinksFromFile() {
		try {
			final Set<String> queued = new HashSet<String>(FileUtils.readLines((new File("queued.txt"))));
			linksQueue.addAllIfNotVisited(queued);
			System.out.println("Found " + queued.size() + " queued links; queue size: " + linksQueue.getSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void submitScrapingTasksWhileThereIsEnoughWork() {
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

				long sleepInterval = new Random(System.currentTimeMillis()).nextInt(500);
				Thread.sleep(sleepInterval);
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
			adPageScrapingThreadPool.submit(new AdPageContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void submitContactScrapingTask(final String link) {
		try {
			contactScrapingCompletionService.submit(new ContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveResults() throws FileNotFoundException, IOException {
		for (int i = 0; i < queuedLinks.size(); i++) {
			try {
				final Future<Contact> future = contactScrapingCompletionService.poll(20, TimeUnit.SECONDS);
				Contact contact = future.get();

				if (contact.isValid()) {
					System.out.println("Added contact " + contact);
					scrapedItems.add(contact);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		final List<Contact> forSorting = new ArrayList<>(scrapedItems);
		Collections.sort(forSorting);

		File contactsFile = new File("contacts.txt");
		FileUtils.writeLines(contactsFile, forSorting);
	}

	private void saveVisitedLinksToFile() {
		try {
			System.out.println("saveVisitedLinksToFile()");
			File visitedFile = new File("visited.txt");
			System.out.println(visitedFile.getAbsolutePath());
			FileUtils.writeLines(visitedFile, linksQueue.getVisitedLinks());
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

			File queuedFile = new File("queued.txt");
			System.out.println(queuedFile.getAbsolutePath());
			FileUtils.writeLines(queuedFile, temp);

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
