package com.fingy.scrape;

public class ScrapeResult {

	private final int queueSize;
	private final int scrapeSize;

	public ScrapeResult(int queueSize, int scrapeSize) {
		this.queueSize = queueSize;
		this.scrapeSize = scrapeSize;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public int getScrapeSize() {
		return scrapeSize;
	}
}
