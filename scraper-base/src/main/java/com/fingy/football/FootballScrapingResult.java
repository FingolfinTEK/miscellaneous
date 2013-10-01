package com.fingy.football;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class FootballScrapingResult {

	private Set<MatchesByDay> matches;

	public FootballScrapingResult() {
		matches = new LinkedHashSet<MatchesByDay>();
	}

	public FootballScrapingResult(MatchesByDay matchesByDay) {
		matches = new LinkedHashSet<MatchesByDay>();
		matches.add(matchesByDay);
	}

	public FootballScrapingResult(Collection<MatchesByDay> matches) {
		matches = new LinkedHashSet<MatchesByDay>(matches);
	}

	public void aggregate(Collection<FootballScrapingResult> results) {
		for (FootballScrapingResult result : results)
			matches.addAll(result.matches);
	}

	public Collection<MatchesByDay> getMatchesByDay() {
		return matches;
	}

}
