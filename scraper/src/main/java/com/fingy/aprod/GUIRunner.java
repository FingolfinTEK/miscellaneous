package com.fingy.aprod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.scrape.security.util.TorUtil;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JTextPane;
import javax.swing.JPanel;

public class GUIRunner extends JFrame {

	private static final long serialVersionUID = 1L;

	private static boolean shouldUseTor = true;

	private static Process scraperProcess;
	private static Thread outputStreamPrinter;
	private static Thread errorStreamPrinter;

	private JTextField txtCotactstxt;
	private File contacts;

	public GUIRunner() {
		getContentPane().setLayout(
				new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));

		JLabel lblOutputFileName = new JLabel("Output file name:");
		getContentPane().add(lblOutputFileName, "2, 2, right, default");

		txtCotactstxt = new JTextField();
		txtCotactstxt.setEditable(false);
		txtCotactstxt.setText("cotacts.txt");
		getContentPane().add(txtCotactstxt, "4, 2, fill, default");
		txtCotactstxt.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(false);

				int selectedOption = fileChooser.showSaveDialog(GUIRunner.this);
				if (selectedOption == JFileChooser.APPROVE_OPTION) {
					File chosen = fileChooser.getSelectedFile();
					if (chosen != null) {
						contacts = chosen;
					}
				}
			}
		});
		getContentPane().add(btnBrowse, "6, 2");

		JTextPane textPane = new JTextPane();
		getContentPane().add(textPane, "4, 4, 3, 1, default, fill");

		JPanel panel = new JPanel();
		getContentPane().add(panel, "4, 6, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JButton btnStart = new JButton("Start scrape");
		panel.add(btnStart, "2, 2");

		JButton btnStopScrape = new JButton("Stop scrape");
		panel.add(btnStopScrape, "4, 2");
	}

	public static void main(String[] args) throws ExecutionException, IOException, InterruptedException {
		setUpTorIfNeeded(args);
		scrapeWhileThereAreResults();
		stopTor();
	}

	private static void setUpTorIfNeeded(String[] args) {
		if (shouldUseTor) {
			TorUtil.stopTor();
			TorUtil.startAndUseTorAsProxy();
			sleep();
		} else {
			TorUtil.disableSocksProxy();
		}
	}

	private static void sleep() {
		try {
			System.out.println("Waiting 45 seconds for Tor to start");
			Thread.sleep(45000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
		int count = 0;
		do {
			count = runScraper();
			shutDown();
			TorUtil.requestNewIdentity();
			sleep();
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

		System.out.println("Scraper finished with count " + scraperProcess.waitFor());
		return scraperProcess.exitValue();
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
		if (shouldUseTor) {
			cmd.append(TorUtil.getTorProxyVMArguments()).append(" ");
		}

		final List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String argument : vmArguments)
			if (argument.startsWith("-D"))
				cmd.append(argument).append(" ");
	}

	private static void stopTor() {
		if (shouldUseTor) {
			TorUtil.stopTor();
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
