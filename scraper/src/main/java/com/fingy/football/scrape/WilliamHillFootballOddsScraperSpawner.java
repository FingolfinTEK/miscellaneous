package com.fingy.football.scrape;

import java.util.Arrays;
import java.util.Collection;

import com.fingy.scrape.spawner.ScraperSpawner;

public class WilliamHillFootballOddsScraperSpawner implements ScraperSpawner<WilliamHillFootballOddsScraper> {

	private static final String TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/0/Football.html";
	private static final String TOMORROW_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/1/Football.html";
	private static final String TWO_DAYS_FROM_TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/2/Football.html";
	private static final String THREE_DAYS_FROM_TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/3/Football.html";
	private static final String FOUR_DAYS_FROM_TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/4/Football.html";
	private static final String FIVE_DAYS_FROM_TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/5/Football.html";
	private static final String SIX_DAYS_FROM_TODAY_URL = "http://sports.williamhill.com/bet/en-gb/betting/y/5/tm/6/Football.html";

	public Collection<WilliamHillFootballOddsScraper> spawn() {
		return Arrays.asList(
			new WilliamHillFootballOddsScraper(TODAY_URL),
			new WilliamHillFootballOddsScraper(TOMORROW_URL),
			new WilliamHillFootballOddsScraper(TWO_DAYS_FROM_TODAY_URL),
			new WilliamHillFootballOddsScraper(THREE_DAYS_FROM_TODAY_URL),
			new WilliamHillFootballOddsScraper(FOUR_DAYS_FROM_TODAY_URL),
			new WilliamHillFootballOddsScraper(FIVE_DAYS_FROM_TODAY_URL),
			new WilliamHillFootballOddsScraper(SIX_DAYS_FROM_TODAY_URL));
	}

}
