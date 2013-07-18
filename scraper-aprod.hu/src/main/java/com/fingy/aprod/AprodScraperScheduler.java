package com.fingy.aprod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.aprod.scrape.AdPageContactJsoupScraper;
import com.fingy.aprod.scrape.ContactJsoupScraper;
import com.fingy.aprod.scrape.FirstAdPageJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class AprodScraperScheduler {
	private static final String AD_LINK_REGEX = ".*aprod\\.hu/hirdetes/.*";

	private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 60;
	private static final int CATEGORY_TIMEOUT = 20000;
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ExecutorService adPageScrapingThreadPool;
	private ExecutorService contactScrapingThreadPool;
	private ExecutorCompletionService<Contact> contactScrapingCompletionService;

	private ScraperLinksQueue linksQueue;
	private Set<String> queuedLinks;
	private Set<Contact> scrapedItems;

	private String initialUrl;
	private String initialSeeksUrl;
	private File contactsFile;
	private File visitedFile;
	private File queuedFile;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			ExecutionException {
		ScrapeResult result = new AprodScraperScheduler(args[0], args[1], args[2], args[3]).doScrape();
		System.exit(result.getQueueSize());
	}

	public AprodScraperScheduler(final String startingUrl, final String contactsFilePath, final String visitedFilePath,
			final String queuedFilePath) {
		adPageScrapingThreadPool = createThreadPool(6);
		contactScrapingThreadPool = createThreadPool(4);
		contactScrapingCompletionService = new ExecutorCompletionService<>(contactScrapingThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedLinks = new LinkedHashSet<>();
		scrapedItems = new LinkedHashSet<>();

		initialUrl = startingUrl;
		initialSeeksUrl = startingUrl + "&search[offer_seek]=seek";
		contactsFile = new File(contactsFilePath);
		visitedFile = new File(visitedFilePath);
		queuedFile = new File(queuedFilePath);
	}

	public ScrapeResult doScrape() {
		int queuedSize = 0;
		try {
			loadContactsFromFile();
			loadVisitedLinksFromFile();
			loadQueuedLinksFromFile();

			adPageScrapingThreadPool.submit(new FirstAdPageJsoupScraper(initialUrl, linksQueue));
			adPageScrapingThreadPool.submit(new FirstAdPageJsoupScraper(initialSeeksUrl, linksQueue));

			submitScrapingTasksWhileThereIsEnoughWork();
			awaitTerminationOfTheTasks();
			collectAndSaveResults();
		} catch (Exception e) {
			logger.error("Exception occured", e);
		} finally {
			saveVisitedLinksToFile();
			queuedSize = saveQueuedLinksToFile();
		}

		logger.trace("Scraped items: " + scrapedItems.size());
		return new ScrapeResult(queuedSize, scrapedItems.size());
	}

	private void loadContactsFromFile() {
		try {
			final List<String> lines = FileUtils.readLines(contactsFile);
			for (String line : lines) {
				scrapedItems.add(Contact.fromString(line));
			}
			logger.trace("Loaded " + lines.size() + " contacts");
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
	}

	private void loadVisitedLinksFromFile() {
		try {
			final Set<String> visited = new HashSet<String>(FileUtils.readLines(visitedFile));
			linksQueue.markAllVisited(visited);
			logger.trace("Found " + visited.size() + " visited links");
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
	}

	private void loadQueuedLinksFromFile() {
		try {
			final Set<String> queued = new HashSet<String>(FileUtils.readLines(queuedFile));
			linksQueue.addAllIfNotVisited(queued);
			logger.trace("Found " + queued.size() + " queued links; queue size: " + linksQueue.getSize());
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
	}

	private void submitScrapingTasksWhileThereIsEnoughWork() {
		AdultItemJsoupScraper.setSessionExpired(false);

		while (stillHaveLinksToBeScraped()) {
			if (AbstractAdultItemJsoupScraper.isSessionExpired()) {
				logger.trace("Session expired, breaking");
				break;
			}

			try {
				String link = linksQueue.take();

				if (isLinkForAdPage(link)) {
					queuedLinks.add(link);
					submitContactScrapingTask(link);
				} else {
					submitAdPageScrapingTask(link);
				}
			} catch (InterruptedException e) {
				logger.error("Exception occured", e);
				break;
			}
		}
	}

	private boolean isLinkForAdPage(String link) {
		return Pattern.matches(AD_LINK_REGEX, link);
	}

	private boolean stillHaveLinksToBeScraped() {
		return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
	}

	private void submitAdPageScrapingTask(final String link) {
		try {
			adPageScrapingThreadPool.submit(new AdPageContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			logger.error("Exception occured", e);
		}
	}

	private void submitContactScrapingTask(final String link) {
		try {
			contactScrapingCompletionService.submit(new ContactJsoupScraper(link, linksQueue));
		} catch (Exception e) {
			logger.error("Exception occured", e);
		}
	}

	private void awaitTerminationOfTheTasks() {
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(
			adPageScrapingThreadPool,
			DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
			TimeUnit.MINUTES);
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(
			contactScrapingThreadPool,
			DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES,
			TimeUnit.MINUTES);
	}

	private void collectAndSaveResults() throws FileNotFoundException, IOException {
		collectResults();

		final List<Contact> forSorting = new ArrayList<>(scrapedItems);
		Collections.sort(forSorting);
		FileUtils.writeLines(contactsFile, forSorting);
	}

	private void collectResults() {
		long timeout = 10;
		for (int i = 0; i < queuedLinks.size(); i++) {
			try {
				final Future<Contact> future = contactScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
				Contact contact = future.get();

				if (contact.isValid()) {
					logger.trace("Added contact " + contact);
					scrapedItems.add(contact);
				}
			} catch (Exception e) {
				timeout = 0;
			}
		}
	}

	private void saveVisitedLinksToFile() {
		try {
			FileUtils.writeLines(visitedFile, linksQueue.getVisitedLinks());
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
	}

	private int saveQueuedLinksToFile() {
		try {
			Set<String> temp = new HashSet<String>();
			temp.addAll(linksQueue.getQueuedLinks());
			temp.addAll(queuedLinks);
			temp.removeAll(linksQueue.getVisitedLinks());

			FileUtils.writeLines(queuedFile, temp);
			return temp.size();
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}

		return 0;
	}

	private ExecutorService createThreadPool(int processorMultiplier) {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * processorMultiplier, Integer.MAX_VALUE, 1,
				TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}
}
