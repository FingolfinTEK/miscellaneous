package com.fingy.football;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MatchesByDay {

	private final String date;
	private final Set<MatchesByCompetition> matches;

	public MatchesByDay(String date) {
		this(date, Collections.<MatchesByCompetition> emptyList());
	}

	public MatchesByDay(String date, MatchesByCompetition match) {
		this(date, Collections.<MatchesByCompetition> emptyList());
		this.matches.add(match);
	}

	public MatchesByDay(String date, Collection<MatchesByCompetition> matches) {
		this.date = date;
		this.matches = new LinkedHashSet<MatchesByCompetition>(matches);
	}

	public String getDate() {
		return date;
	}

	public Collection<MatchesByCompetition> getMatchesByCompetition() {
		return matches;
	}

	public void addCompetition(MatchesByCompetition matchesByCompetition) {
		matches.add(matchesByCompetition);
	}

}
