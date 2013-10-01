package com.fingy.football;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MatchOdds {

	private final String matchName;
	private final String competition;
	private final String date;
	private final String oddsThatHomeWins;
	private final String oddsForDrawMatch;
	private final String oddsThatAwayWins;

	public MatchOdds(String matchName, String competition, String date, String time, String oddsThatHomeWins,
			String oddsForDrawMatch, String oddsThatAeayWins) {
		this.matchName = matchName;
		this.competition = competition;
		this.date = date;
		this.oddsThatHomeWins = oddsThatHomeWins;
		this.oddsForDrawMatch = oddsForDrawMatch;
		this.oddsThatAwayWins = oddsThatAeayWins;
	}

	public String getMatchName() {
		return matchName;
	}

	public String getCompetition() {
		return competition;
	}

	public String getDate() {
		return date;
	}

	public String getOddsThatHomeWins() {
		return oddsThatHomeWins;
	}

	public String getOddsForDrawMatch() {
		return oddsForDrawMatch;
	}

	public String getOddsThatAwayWins() {
		return oddsThatAwayWins;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(matchName);
		hashCodeBuilder.append(competition);
		hashCodeBuilder.append(date);
		hashCodeBuilder.append(oddsThatHomeWins);
		hashCodeBuilder.append(oddsForDrawMatch);
		hashCodeBuilder.append(oddsThatAwayWins);
		return hashCodeBuilder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		MatchOdds other = (MatchOdds) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(matchName, other.matchName);
		equalsBuilder.append(competition, other.competition);
		equalsBuilder.append(date, other.date);
		equalsBuilder.append(oddsThatHomeWins, other.oddsThatHomeWins);
		equalsBuilder.append(oddsForDrawMatch, other.oddsForDrawMatch);
		equalsBuilder.append(oddsThatAwayWins, other.oddsThatAwayWins);
		return equalsBuilder.isEquals();
	}
}
