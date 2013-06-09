/**
 * @author Fingy
 *
 */
package com.fingy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class GoogleSearchScript {

    private static final int    NUM_DAYS          = 5;

    private static final String START_QUERY       = "&start=%d";

    private static final String DATE_SEARCH_QUERY = "&tbs=cdr:1,cd_min:%s,cd_max:%s";

    private static final String BASE_SEARCH_QUERY = "https://www.google.com/search?num=100&lr=&hl=en&as_qdr=all&pws=0&biw=1333&bih=618&q=github+api+oauth&oq=github+api+oauth"
                                                          + "&gs_l=serp.3..0l3j0i22i30l7.3439.8439.0.8766.8.7.1.0.0.0.231.855.3j3j1.7.0...0.0...1c.1.15.serp.5XqCgUzeSqU";

    private final WebDriver     driver;
    private final String        searchUrl;

    public static void main(final String[] args) {
        final GoogleSearchScript googleSearchScript = new GoogleSearchScript(BASE_SEARCH_QUERY);

        googleSearchScript.waitForTheBrowserToStart();
        googleSearchScript.doWork();
        googleSearchScript.finishWork();
    }

    public GoogleSearchScript(final String googleSearchUrl) {
        driver = new FirefoxDriver();
        searchUrl = googleSearchUrl;
    }

    public void waitForTheBrowserToStart() {
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    }

    public void finishWork() {
        driver.quit();
    }

    public void doWork() {
        final List<String> linksToVisit = new ArrayList<>();

        for (int i = 0; i <= NUM_DAYS; i++) {
            final Calendar ithDay = getIthDayFromToday(-i);

            for (int j = 0; j < 10; j++) {
                loadPage(ithDay, j);
                extractLinksInto(linksToVisit);
            }
        }

        visitLinks(linksToVisit);
    }

    private Calendar getIthDayFromToday(final int i) {
        final Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, i);
        return currentDate;
    }

    private void loadPage(final Calendar ithDay, final int page) {
        driver.get(getUrlForDayAndPage(ithDay, page));
        waitForThePageToLoad();
    }

    private void extractLinksInto(final List<String> linksToVisit) {
        final List<WebElement> linkElements = driver.findElements(By.cssSelector("h3.r a"));

        for (final WebElement element : linkElements) {
            linksToVisit.add(element.getAttribute("href"));
        }
    }

    private String getUrlForDayAndPage(final Calendar date, final int page) {
        final String currentDateString = new SimpleDateFormat("MM/dd/yyyy").format(date.getTime());
        final String dateSearchQuery = String.format(DATE_SEARCH_QUERY, currentDateString, currentDateString);
        final String startQuery = String.format(START_QUERY, page * 100);

        return searchUrl + dateSearchQuery + startQuery;
    }

    private void waitForThePageToLoad() {
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
    }

    private void visitLinks(final List<String> linksToVisit) {
        for (final String link : linksToVisit) {
            driver.get(link);
            waitForThePageToLoad();
        }
    }
}
