package com.fingy.aprod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.scrape.security.util.TorUtil;

public class AprodScraperRunner {

	private static boolean shouldUseTor = false;

	private static Process scraperProcess;

	private static Thread outputStreamPrinter;
	private static Thread errorStreamPrinter;

	public static void main(String[] args) throws ExecutionException, IOException, InterruptedException {
		scrapeWhileThereAreResults();
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

			count = runScraper();

			if (shouldUseTor) {
				TorUtil.stopTor();
			}

			Thread.sleep(60000);
		} while (count > 0);
	}

	public static int runScraper() throws IOException, InterruptedException {
		addShutdownHookToCloseTheProcess();

		String command = generateCommand();
		scraperProcess = Runtime.getRuntime().exec(command);

		outputStreamPrinter = new InputStreamPrinterThread(scraperProcess.getInputStream());
		outputStreamPrinter.start();

		errorStreamPrinter = new InputStreamPrinterThread(scraperProcess.getErrorStream());
		errorStreamPrinter.start();

		scraperProcess.waitFor();
		return scraperProcess.exitValue();
	}

	private static String generateCommand() {
		final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();

		final StringBuilder cmd = new StringBuilder();
		cmd.append("java ");
		cmd.append("-cp \"").append(classPath).append("\" ");
		appendVMArguments(cmd);
		cmd.append(AprodScraperScheduler.class.getName());

		return cmd.toString();
	}

	private static void appendVMArguments(final StringBuilder cmd) {
		final List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String argument : vmArguments)
			if (argument.startsWith("-D"))
				cmd.append(argument).append(" ");
	}

	protected static void addShutdownHookToCloseTheProcess() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutDown();
			}
		});
	}

	public static void shutDown() {
		if (outputStreamPrinter != null) {
			outputStreamPrinter.interrupt();
		}

		if (errorStreamPrinter != null) {
			errorStreamPrinter.interrupt();
		}

		if (scraperProcess != null) {
			scraperProcess.destroy();
		}
	}

	private static final class InputStreamPrinterThread extends Thread {
		private final Logger logger = LoggerFactory.getLogger(getClass());
		private final InputStream inputStream;

		private InputStreamPrinterThread(final InputStream inputStream) {
			this.inputStream = inputStream;
			setDaemon(true);
		}

		@Override
		public void run() {
			while (!interrupted()) {
				try {
					final BufferedReader stream = new BufferedReader(new InputStreamReader(inputStream));
					final String readLine = stream.readLine();
					if (readLine != null && StringUtils.isNotBlank(readLine)) {
						logger.debug(readLine);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
