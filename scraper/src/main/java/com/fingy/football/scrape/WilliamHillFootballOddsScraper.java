package com.fingy.football.scrape;

import java.util.Collection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.fingy.football.MatchOdds;
import com.fingy.football.MatchesByCompetition;
import com.fingy.football.MatchesByDay;

public class WilliamHillFootballOddsScraper extends AbstractMatchesByDayJsoupScraper {

	private MatchesByDay matchesByDay;

	public WilliamHillFootballOddsScraper(String scrapeUrl) {
		super(scrapeUrl);
	}

	@Override
	protected MatchesByDay populateWorkbookFromPage(Document page) {
		final String date = extractDateFromPage(page);
		matchesByDay = new MatchesByDay(date);

		final Element matchesElement = page.getElementById("ip_sport_0_types");
		return extractMatchesFromElement(matchesElement);
	}

	private String extractDateFromPage(Document page) {
		return page.getElementById("selectedDailyMatch").text();
	}

	private MatchesByDay extractMatchesFromElement(Element matchesElement) {
		for (Node child : matchesElement.childNodes()) {
			final Element competitionElement = (Element) child;
			
			final String competitionName = extractCompetitionNameFromCompetitionElement(competitionElement);
			final Collection<MatchOdds> matches = extractMatchesFromCompetitionElement(competitionElement);

			matchesByDay.addCompetition(new MatchesByCompetition(competitionName, matches));
		}

		return matchesByDay;
	}

	private String extractCompetitionNameFromCompetitionElement(final Element competitionElement) {
		return competitionElement.getElementsByTag("h3").first().text();
	}

	private Collection<MatchOdds> extractMatchesFromCompetitionElement(Element competitionElement) {
		
		return null;
	}

}
