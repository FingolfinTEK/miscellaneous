package com.fingy.adultwholesale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.scrape.JsoupImageDownloader;

public class AdultWholesaleImageDownloaderScheduler {

	private static final String IMAGE_URL = "http://www.adultwholesaledirect.com/customer/displayimagewm.php?id=";
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private Set<String> imageIdsToDownload;
	private ExecutorService imageScrapingThreadPool;
	private String imageIdsFilePath;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			ExecutionException {
		new AdultWholesaleImageDownloaderScheduler("wholesale-images.txt").doScrape();
	}

	public AdultWholesaleImageDownloaderScheduler(final String imageIdsFile) {
		imageScrapingThreadPool = createDefaultThreadPool(); // Executors.newCachedThreadPool();
		imageIdsFilePath = imageIdsFile;
	}

	public void doScrape() throws ExecutionException, IOException {
		System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
		System.getProperties().setProperty("socksProxyPort", "9150");

		imageIdsToDownload = new HashSet<String>(FileUtils.readLines(new File(imageIdsFilePath)));

		for (String imageId : imageIdsToDownload) {
			String imagePath = "C:/Users/Fingy/Desktop/wholesale/" + imageId;
			File imageFile = new File(imagePath + ".jpeg");
			if (!imageFile.exists()) {
				imageScrapingThreadPool.execute(new JsoupImageDownloader(IMAGE_URL + imageId, imagePath, Collections
						.<String, String> emptyMap()));
			}
		}

		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(imageScrapingThreadPool, 30, TimeUnit.MINUTES);

	}

	private ExecutorService createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 40, Integer.MAX_VALUE, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>());
	}
}
