package com.fingy.scrape.context;

public interface DetailsLoader<T> {

    T loadFromCSVLine(String line);
}
