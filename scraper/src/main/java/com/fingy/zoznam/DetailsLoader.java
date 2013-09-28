package com.fingy.zoznam;

public interface DetailsLoader<T> {

    T loadFromCSVLine(String line);
}
