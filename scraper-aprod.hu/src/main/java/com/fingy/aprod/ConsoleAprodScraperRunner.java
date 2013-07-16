package com.fingy.aprod;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.fingy.scrape.security.util.TorUtil;

public class ConsoleAprodScraperRunner {

	private static final String USE_TOR_PARAM_NAME = "useTor";

	private static boolean shouldUseTor = true;

	public static void main(String[] args) throws ExecutionException, IOException, InterruptedException {
		setUpTorIfNeeded(args);
		scrapeWhileThereAreResults();
		stopTor();
	}

	private static void setUpTorIfNeeded(String[] args) {
		if (args.length == 1) {
			shouldUseTor = USE_TOR_PARAM_NAME.equals(args[0]);
		}

		if (shouldUseTor) {
			TorUtil.stopTor();
			TorUtil.startAndUseTorAsProxy();
			sleep(45000);
		} else {
			TorUtil.disableSocksProxy();
		}
	}

	private static void sleep(int millis) {
		try {
			System.out.println(String.format("Waiting %d seconds", millis / 1000));
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
		int queueSize = 0;
		do {
			ScrapeResult result = new AprodScraperScheduler(Category.ALL.getLink(), "contacts.txt", "visited.txt", "queued.txt").doScrape();
			queueSize = result.getQueueSize();
			TorUtil.requestNewIdentity();
			sleep(30000);
		} while (queueSize > 0);
	}

	private static void stopTor() {
		if (shouldUseTor) {
			TorUtil.stopTor();
		}
	}

}
