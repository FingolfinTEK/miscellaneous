package com.fingy.football;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MatchesByCompetition {

	private final String competitionName;
	private final Set<MatchOdds> matches;
	
	public MatchesByCompetition(String competitionName) {
		this(competitionName, Collections.<MatchOdds> emptyList());
	}

	public MatchesByCompetition(String competitionName, MatchOdds match) {
		this(competitionName, Collections.<MatchOdds> emptyList());
		this.matches.add(match);
	}

	public MatchesByCompetition(String competitionName, Collection<MatchOdds> matches) {
		this.competitionName = competitionName;
		this.matches = new LinkedHashSet<MatchOdds>(matches);
	}

	public void addMatch(MatchOdds odds) {
		matches.add(odds);
	}

	public String getDate() {
		return competitionName;
	}

	public Collection<MatchOdds> getMatches() {
		return matches;
	}

}
