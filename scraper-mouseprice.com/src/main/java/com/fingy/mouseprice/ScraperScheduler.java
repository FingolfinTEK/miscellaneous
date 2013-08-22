package com.fingy.mouseprice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.adultwholesale.scrape.AbstractAdultItemJsoupScraper;
import com.fingy.adultwholesale.scrape.AdultItemJsoupScraper;
import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.mouseprice.scrape.HousePricesScraper;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.queue.ScraperLinksQueue;

public class ScraperScheduler {

	private static final String START_URL = "http://www.mouseprice.com/house-prices/";

	private static final int DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES = 5;
	private static final int CATEGORY_TIMEOUT = 30000;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ExecutorService detailLinksScrapingThreadPool;
	private final ExecutorService detailsScrapingThreadPool;
	private final ExecutorCompletionService<List<RealEstateInfo>> detailsScrapingCompletionService;

	private final ScraperLinksQueue linksQueue;
	private final Set<String> queuedLinks;
	private final Set<RealEstateInfo> scrapedItems;

	private List<String> zipsToScrape;

	private final File detailsFile;
	private final File visitedFile;
	private final File queuedFile;

	public ScraperScheduler(final List<String> zips, final String detailsFilePath, final String visitedFilePath, final String queuedFilePath) {
		detailLinksScrapingThreadPool = Executors.newSingleThreadExecutor();
		detailsScrapingThreadPool = Executors.newSingleThreadExecutor();
		detailsScrapingCompletionService = new ExecutorCompletionService<>(detailsScrapingThreadPool);

		linksQueue = new ScraperLinksQueue();
		queuedLinks = new LinkedHashSet<>();
		scrapedItems = new LinkedHashSet<>();

		zipsToScrape = zips;

		detailsFile = new File(detailsFilePath);
		visitedFile = new File(visitedFilePath);
		queuedFile = new File(queuedFilePath);
	}

	public ScrapeResult doScrape() {
		int queuedSize = 0;
		try {
			loadDetailsFromFile();
			loadVisitedLinksFromFile();
			loadQueuedLinksFromFile();

			createInitialScrapingTasks();
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

	private void createInitialScrapingTasks() {
		for (String zip : zipsToScrape) {
			linksQueue.addIfNotVisited(START_URL + zip.replace(" ", "+"));
		}
	}

	private void loadDetailsFromFile() {
		try {
			final List<String> lines = FileUtils.readLines(detailsFile);
			for (String line : lines) {
				scrapedItems.add(RealEstateInfo.fromString(line));
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
		AdultItemJsoupScraper.setScrapeCompromised(false);

		while (stillHaveLinksToBeScraped()) {
			if (AbstractAdultItemJsoupScraper.isScrapeCompromised()) {
				logger.trace("Session expired, breaking");
				break;
			}

			try {
				String link = linksQueue.take();
				queuedLinks.add(link);
				detailsScrapingCompletionService.submit(new HousePricesScraper(link, linksQueue));
				// Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Exception occured", e);
				break;
			}
		}
	}

	private boolean stillHaveLinksToBeScraped() {
		return !linksQueue.delayedIsEmpty(CATEGORY_TIMEOUT);
	}

	private void awaitTerminationOfTheTasks() {
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(detailLinksScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES, TimeUnit.MINUTES);
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(detailsScrapingThreadPool, DEFAULT_TERMINATION_AWAIT_INTERVAL_MINUTES, TimeUnit.MINUTES);
	}

	private void collectAndSaveResults() throws FileNotFoundException, IOException {
		collectResults();
		FileUtils.writeLines(detailsFile, scrapedItems);
	}

	private void collectResults() {
		long timeout = 10;
		for (int i = 0; i < queuedLinks.size(); i++) {
			try {
				final Future<List<RealEstateInfo>> future = detailsScrapingCompletionService.poll(timeout, TimeUnit.SECONDS);
				final List<RealEstateInfo> results = future.get();
				scrapedItems.addAll(results);
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
}
