package com.fingy.aprod;

import com.fingy.aprod.criteria.Category;
import com.fingy.aprod.criteria.City;
import com.fingy.gui.AppendableJTextArea;
import com.fingy.scrape.context.ScrapeContext;
import com.fingy.scrape.context.ScrapeResult;
import com.fingy.scrape.jsoup.AbstractJsoupScraper;
import com.fingy.scrape.security.ProxyBasedScrapeDetectionOverrider;
import com.fingy.scrape.security.TorNetworkProxyBasedScrapeDetectionOverride;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AprodHuGUIRunner extends JFrame {

    private static final int RETRY_COUNT = 5;
    private static final String VISITED_TXT_FILE_NAME = "visited.txt";
    private static final String QUEUED_TXT_FILE_NAME = "queued.txt";
    private static final String DEFAULT_CONTACTS_FILE = "contacts.txt";

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean shouldStop;

    private final JTextField contactsFilePath;
    private final AppendableJTextArea infoPane;
    private final JComboBox<Category> categoryCombo;
    private final JComboBox<City> cityCombo;
    private final JButton btnStartScrape;
    private final JButton btnStopScrape;

    private File contacts = new File(DEFAULT_CONTACTS_FILE);
    private ScraperWorker scraperWorker;

    public AprodHuGUIRunner() {
        setPreferredSize(new Dimension(600, 400));
        setTitle("aprod.hu Scraper");
        getContentPane().setLayout(new FormLayout(
                new ColumnSpec[]{FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                        FormFactory.RELATED_GAP_COLSPEC,}, new RowSpec[]{FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("max(11dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,}));

        JLabel lblOutputFileName = new JLabel("Output file name:");
        getContentPane().add(lblOutputFileName, "2, 2, right, default");

        contactsFilePath = new JTextField();
        contactsFilePath.setEditable(false);
        contactsFilePath.setText(DEFAULT_CONTACTS_FILE);
        getContentPane().add(contactsFilePath, "4, 2, fill, default");
        contactsFilePath.setColumns(10);

        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(false);

                int selectedOption = fileChooser.showSaveDialog(AprodHuGUIRunner.this);
                if (selectedOption == JFileChooser.APPROVE_OPTION) {
                    File chosen = fileChooser.getSelectedFile();
                    if (chosen != null) {
                        contacts = chosen;
                        contactsFilePath.setText(chosen.getAbsolutePath());
                    }
                }
            }
        });
        getContentPane().add(btnBrowse, "6, 2");

        JLabel lblScrapeCity = new JLabel("Scrape city:");
        getContentPane().add(lblScrapeCity, "2, 4, right, default");

        cityCombo = new JComboBox<>();
        cityCombo.setModel(new DefaultComboBoxModel<>(City.values()));
        cityCombo.setSelectedItem(City.BUDAPEST);
        getContentPane().add(cityCombo, "4, 4, fill, default");

        JLabel lblScrapeCategory = new JLabel("Scrape category:");
        getContentPane().add(lblScrapeCategory, "2, 6, right, default");

        categoryCombo = new JComboBox<>();
        categoryCombo.setModel(new DefaultComboBoxModel<>(Category.values()));
        categoryCombo.setSelectedItem(Category.ALL);
        getContentPane().add(categoryCombo, "4, 6, fill, default");

        JLabel lblActivityLog = new JLabel("Activity log:");
        getContentPane().add(lblActivityLog, "2, 8, default, top");

        infoPane = new AppendableJTextArea();
        infoPane.setRows(10);
        infoPane.setEditable(false);
        infoPane.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(infoPane), "4, 8, 3, 1, default, fill");

        JPanel panel = new JPanel();
        getContentPane().add(panel, "4, 10, 3, 1, fill, fill");
        panel.setLayout(new FormLayout(new ColumnSpec[]{FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,}, new RowSpec[]{FormFactory.DEFAULT_ROWSPEC,}));

        btnStartScrape = new JButton("Start scrape");
        btnStartScrape.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                btnStartScrape.setEnabled(false);
                btnStopScrape.setEnabled(true);

                scraperWorker = new ScraperWorker();
                scraperWorker.execute();
            }
        });
        panel.add(btnStartScrape, "1, 1");

        btnStopScrape = new JButton("Stop scrape");
        btnStopScrape.setEnabled(false);
        btnStopScrape.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                shouldStop = true;
                infoPane.appendLine("Stopping scrape process after the current iteration");
                btnStopScrape.setEnabled(false);
                scraperWorker.terminate();
            }
        });
        panel.add(btnStopScrape, "3, 1");

        JButton btnResetState = new JButton("Reset state");
        btnResetState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(AprodHuGUIRunner.this,
                        "This will delete internal data. Are you sure you wish to continue?",
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

        final JButton btnClearLog = new JButton("Clear Log");
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
                if (scraperWorker != null) scraperWorker.terminate();
                System.exit(0);
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
                AprodHuGUIRunner runner = new AprodHuGUIRunner();
                runner.pack();
                runner.setLocationRelativeTo(null);
                runner.setVisible(true);
            }
        });
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

    private final class ScraperWorker extends SwingWorker<Object, Object> {
        private ProxyBasedScrapeDetectionOverrider scrapeDetectionOverrider = new TorNetworkProxyBasedScrapeDetectionOverride();

        @Override
        protected Object doInBackground() throws Exception {
            runScrape();
            return null;
        }

        public void terminate() {
            AbstractJsoupScraper.setScrapeCompromised(true);
            shouldStop = true;
            scrapeDetectionOverrider.destroyContext();
        }

        public void runScrape() {
            try {
                shouldStop = false;
                scrapeDetectionOverrider.initializeContext();

                for (int i = 0; i < RETRY_COUNT; i++) {
                    scrapeWhileThereAreResults();
                }

                scrapeDetectionOverrider.destroyContext();
            } catch (Exception e) {
                logger.error("Exception occured", e);
            }
        }

        private void scrapeWhileThereAreResults() throws ExecutionException, IOException, InterruptedException {
            int queueSize = 1;
            while (queueSize > 0 && !shouldStop) {
                City city = (City) cityCombo.getSelectedItem();
                Category category = (Category) categoryCombo.getSelectedItem();

                infoPane.appendLine("Starting new scrape iteration for city " + city + " and category " + category);

                setUpProxy();
                ScrapeResult result = doScrape(city, category);

                infoPane.appendLine("Finished scrape iteration");
                infoPane.appendLine("Scrape was compromised: " + (AbstractJsoupScraper.isScrapeCompromised() && !shouldStop));
                infoPane.appendLine("Total contacts scraped: " + result.getScrapeSize());

                queueSize = result.getQueueSize();
            }
        }

        private void setUpProxy() {
            scrapeDetectionOverrider.setUpProxy();
            sleep(2000);
        }

        private ScrapeResult doScrape(City city, Category category) {
            String startUrl = String.format(category.getLink(), city.getUrlName());
            ScrapeContext context = new ScrapeContext(contacts.getAbsolutePath(), VISITED_TXT_FILE_NAME, QUEUED_TXT_FILE_NAME, new ContactLoader());
            ScrapeResult result = new AprodScraperScheduler(startUrl, context).doScrape();
            context.save();
            return result;
        }

        @Override
        protected void done() {
            btnStartScrape.setEnabled(true);
            btnStopScrape.setEnabled(false);
            infoPane.appendLine("Finished scraping.");
        }
    }

}
