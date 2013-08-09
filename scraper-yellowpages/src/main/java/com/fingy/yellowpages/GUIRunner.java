package com.fingy.yellowpages;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fingy.gui.AppendableJTextArea;
import com.fingy.scrape.ScrapeResult;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.security.util.TorUtil;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class GUIRunner extends JFrame {

	private static final int RETRY_COUNT = 5;
	private static final String VISITED_TXT_FILE_NAME = "visited.txt";
	private static final String QUEUED_TXT_FILE_NAME = "queued.txt";
	private static final String DEFAULT_COMPANIES_FILE = "companies.txt";

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final boolean shouldUseTor = true;
	private boolean shouldStop;

	private final AppendableJTextArea infoPane;
	private final JTextField contactsFilePath;
	private final JTextField searchLocation;
	private final JTextField searchTerm;
	private final JButton btnStartScrape;
	private final JButton btnStopScrape;
	private final JButton btnClearLog;

	private String term;
	private String location;
	private String contactsFile;

	public GUIRunner() {
		setPreferredSize(new Dimension(600, 400));
		setTitle("YellowPages Scraper");
		getContentPane().setLayout(
				new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
						new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
								RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(11dlu;default)"),
								FormFactory.RELATED_GAP_ROWSPEC, }));

		JLabel lblOutputFileName = new JLabel("Output file name:");
		getContentPane().add(lblOutputFileName, "2, 2, right, default");

		contactsFilePath = new JTextField();
		contactsFilePath.setEditable(false);
		contactsFilePath.setText(DEFAULT_COMPANIES_FILE);
		getContentPane().add(contactsFilePath, "4, 2, fill, default");
		contactsFilePath.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(false);

				int selectedOption = fileChooser.showSaveDialog(GUIRunner.this);
				if (selectedOption == JFileChooser.APPROVE_OPTION) {
					File chosen = fileChooser.getSelectedFile();
					if (chosen != null) {
						contactsFilePath.setText(chosen.getAbsolutePath());
					}
				}
			}
		});
		getContentPane().add(btnBrowse, "6, 2");

		JLabel lblSearchTerm = new JLabel("Search term:");
		getContentPane().add(lblSearchTerm, "2, 4, right, default");

		searchTerm = new JTextField();
		getContentPane().add(searchTerm, "4, 4, fill, default");

		JLabel lblSearchLocation = new JLabel("Search location:");
		getContentPane().add(lblSearchLocation, "2, 6, right, default");

		searchLocation = new JTextField();
		getContentPane().add(searchLocation, "4, 6, fill, default");

		JLabel lblActivityLog = new JLabel("Activity log:");
		getContentPane().add(lblActivityLog, "2, 8, default, top");

		infoPane = new AppendableJTextArea();
		infoPane.setRows(10);
		infoPane.setEditable(false);
		infoPane.setWrapStyleWord(true);
		getContentPane().add(new JScrollPane(infoPane), "4, 8, 3, 1, default, fill");

		JPanel panel = new JPanel();
		getContentPane().add(panel, "4, 10, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
				new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

		btnStartScrape = new JButton("Start scrape");
		btnStartScrape.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				searchTerm.setEnabled(false);
				searchLocation.setEnabled(false);
				btnStartScrape.setEnabled(false);
				btnStopScrape.setEnabled(true);

				term = searchTerm.getText();
				location = searchLocation.getText();
				contactsFile = contactsFilePath.getText();
				new ScraperWorker().execute();
			}
		});
		panel.add(btnStartScrape, "1, 1");

		btnStopScrape = new JButton("Stop scrape");
		btnStopScrape.setEnabled(false);
		btnStopScrape.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				shouldStop = true;
				AbstractJsoupScraper.setScrapeCompromised(true);
				infoPane.appendLine("Stopping scrape process after the current iteration");
				btnStopScrape.setEnabled(false);
			}
		});
		panel.add(btnStopScrape, "3, 1");

		JButton btnResetState = new JButton("Reset state");
		btnResetState.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(GUIRunner.this, "This will delete internal data. Are you sure you wish to continue?",
						"Confirm scraper reset", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					FileUtils.deleteQuietly(new File(VISITED_TXT_FILE_NAME));
					infoPane.appendLine("Deleted visited links");
					FileUtils.deleteQuietly(new File(QUEUED_TXT_FILE_NAME));
					infoPane.appendLine("Deleted queued links");
				}
			}
		});
		panel.add(btnResetState, "5, 1");

		btnClearLog = new JButton("Clear Log");
		btnClearLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				infoPane.setText("");
			}
		});
		panel.add(btnClearLog, "7, 1");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				int option = JOptionPane.showConfirmDialog(GUIRunner.this, "This will exit without saving the current state and data. Are you sure?",
						"Confirm exit", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					TorUtil.stopTor();
					System.exit(0);
				}
			}
		});
	}

	public static void main(final String[] args) throws ExecutionException, IOException, InterruptedException {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException ignored) {
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				GUIRunner runner = new GUIRunner();
				runner.pack();
				runner.setLocationRelativeTo(null);
				runner.setVisible(true);
			}
		});
	}

	public void runScrape() {
		try {
			shouldStop = false;
			setUpTorIfNeeded();

			for (int i = 0; i < RETRY_COUNT; i++) {
				scrapeWhileThereAreResults();
			}

			stopTor();
		} catch (Exception e) {
			logger.error("Exception occured", e);
		}
	}

	private void setUpTorIfNeeded() {
		if (shouldUseTor) {
			TorUtil.stopTor();
			TorUtil.startAndUseTorAsProxy();
			sleep(45000);
		} else {
			TorUtil.disableSocksProxy();
		}
	}

	private void sleep(final int millis) {
		try {
			String sleepMessage = String.format("Waiting %d seconds", millis / 1000);
			infoPane.appendLine(sleepMessage);
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("Exception occured", e);
		}
	}

	private void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
		int queueSize = 1;
		while (queueSize > 0 && !shouldStop) {
			infoPane.appendLine("Starting new scrape iteration for term " + term + " and location " + location);

			ScrapeResult result = new ScraperScheduler(term, location, contactsFile, VISITED_TXT_FILE_NAME, QUEUED_TXT_FILE_NAME).doScrape();

			infoPane.appendLine("Finished scrape iteration");
			infoPane.appendLine("Scrape was compromised: " + (AbstractJsoupScraper.isScrapeCompromised() && !shouldStop));
			infoPane.appendLine("Total contacts scraped: " + result.getScrapeSize());

			queueSize = result.getQueueSize();
			TorUtil.requestNewIdentity();
			sleep(10000);
		}
	}

	private void stopTor() {
		if (shouldUseTor) {
			TorUtil.stopTor();
		}
	}

	private final class ScraperWorker extends SwingWorker<Object, Object> {
		@Override
		protected Object doInBackground() throws Exception {
			runScrape();
			return null;
		}

		@Override
		protected void done() {
			searchTerm.setEnabled(true);
			searchLocation.setEnabled(true);
			btnStartScrape.setEnabled(true);
			btnStopScrape.setEnabled(false);
			infoPane.appendLine("Finished scraping.");
		}
	}

}
