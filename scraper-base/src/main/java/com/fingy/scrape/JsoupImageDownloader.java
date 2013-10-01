package com.fingy.scrape;

import static com.fingy.scrape.jsoup.AbstractJsoupScraper.USER_AGENT;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupImageDownloader implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String imageUrl;
	private final String fileName;
	private final Map<String, String> cookies;

	public JsoupImageDownloader(final String imageUrl, final String fileName, final Map<String, String> cookies) {
		this.imageUrl = imageUrl;
		this.fileName = fileName;
		this.cookies = cookies;
	}

	@Override
	public void run() {
		try {
			logger.debug("Downloading image " + imageUrl);
			Response image = createAndSetUpImageConnection().execute();
			String extension = determineExtensionFromResponse(image);
			FileUtils.writeByteArrayToFile(new File(fileName + extension), image.bodyAsBytes());
		} catch (Exception e) {
			logger.error("Exception downloading image", e);
		}
	}

	private String determineExtensionFromResponse(Response image) {
		String contentType = image.contentType();
		String extension = "." + contentType.replace("image/", "").replaceAll("\\W", "");
		return extension;
	}

	private Connection createAndSetUpImageConnection() {
		return Jsoup.connect(imageUrl).cookies(cookies).userAgent(USER_AGENT).timeout(0).ignoreContentType(true);
	}
}