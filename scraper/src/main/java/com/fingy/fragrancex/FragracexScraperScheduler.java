package com.fingy.fragrancex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.fingy.concurrent.ExecutorsUtil;
import com.fingy.fragrancex.scrape.AbstractExcelWorksheetBasedTaskProducer;
import com.fingy.fragrancex.scrape.FragrancexTask;
import com.fingy.fragrancex.scrape.PerfumeItemJsoupScraper;
import com.fingy.scrape.JsoupImageDownloader;

public class FragracexScraperScheduler {

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private ExecutorService itemScrapintThreadPool;
	private ExecutorCompletionService<PerfumeItem> itemScrapingCompletionService;
	private ThreadPoolExecutor imageScrapingThreadPool;

	private Workbook workbook;

	private String inputFilePath;
	private String outputFilePath;
	private String imageDirPath;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			ExecutionException {
		new FragracexScraperScheduler("C:/Users/Fingy/Desktop/fragrancex.xls",
				"C:/Users/Fingy/Desktop/fragrancex2.xls", "C:/Users/Fingy/Desktop/fragrancex/").doScrape();
	}

	public FragracexScraperScheduler(String inputPath, String outputPath, String imagePath) {
		itemScrapintThreadPool = createDefaultThreadPool();
		itemScrapingCompletionService = new ExecutorCompletionService<>(itemScrapintThreadPool);
		imageScrapingThreadPool = createDefaultThreadPool();
		inputFilePath = inputPath;
		outputFilePath = outputPath;
		imageDirPath = imagePath;
	}

	public void doScrape() throws ExecutionException, IOException {
		produceTasks();
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(itemScrapintThreadPool, 10, TimeUnit.SECONDS);
		saveResultsToExcelAndDownloadImages();
	}

	private void produceTasks() {
		File workbookFile = new File(inputFilePath);
		try {
			produceTasks(workbookFile);
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	private void produceTasks(File workbookFile) throws InvalidFormatException, IOException {
		workbook = WorkbookFactory.create(workbookFile);
		new AbstractExcelWorksheetBasedTaskProducer() {
			@Override
			public void doWithTask(FragrancexTask task) {
				try {
					PerfumeItemJsoupScraper scraperWorker = new PerfumeItemJsoupScraper(task);
					itemScrapingCompletionService.submit(scraperWorker);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}.forWorkbook(workbook).startingFromRowNumber(2).produce();
	}

	private void saveResultsToExcelAndDownloadImages() throws FileNotFoundException, IOException {
		while (true) {
			try {
				final Future<PerfumeItem> future = itemScrapingCompletionService.poll(15, TimeUnit.SECONDS);
				final PerfumeItem item = future.get();

				submitImageDownloadingTaskForItem(item);
				updateWorksheet(item);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		try (FileOutputStream outputStream = new FileOutputStream(new File(outputFilePath))) {
			System.out.println("Writing workbook to file");
			workbook.write(outputStream);
			System.out.println("Finished writing workbook to file");
		} catch (IOException e) {
			e.printStackTrace();
		}

		ExecutorService executorService = imageScrapingThreadPool;
		ExecutorsUtil.shutDownExecutorServiceAndAwaitTermination(executorService, 5, TimeUnit.MINUTES);
	}

	private void submitImageDownloadingTaskForItem(final PerfumeItem item) {
		String fileName = imageDirPath + item.getId();
		Map<String, String> cookies = Collections.<String, String> emptyMap();
		imageScrapingThreadPool.execute(new JsoupImageDownloader(item.getImageUrl(), fileName, cookies));
	}

	private ThreadPoolExecutor createDefaultThreadPool() {
		return new ThreadPoolExecutor(AVAILABLE_PROCESSORS * 6, Integer.MAX_VALUE, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>());
	}

	private void updateWorksheet(final PerfumeItem item) {
		Row row = workbook.getSheetAt(0).getRow(item.getRowInWorksheet());
		row.createCell(9).setCellValue(item.getPrice());
		row.createCell(10).setCellValue(item.getDescription());
		row.createCell(11).setCellValue(item.getImageUrl());
	}

}
