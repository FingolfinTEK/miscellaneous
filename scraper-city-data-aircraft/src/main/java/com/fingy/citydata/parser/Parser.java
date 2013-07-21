package com.fingy.citydata.parser;

public interface Parser<TypeToParse> {

    TypeToParse parse(String stringToParse);

}
