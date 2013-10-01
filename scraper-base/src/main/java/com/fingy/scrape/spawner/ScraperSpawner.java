package com.fingy.scrape.spawner;

import java.util.Collection;

import com.fingy.scrape.AbstractScraper;

public interface ScraperSpawner<T extends AbstractScraper<?>> {
	
	Collection<T> spawn();

}
