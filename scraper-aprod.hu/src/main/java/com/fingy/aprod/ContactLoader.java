package com.fingy.aprod;

import com.fingy.scrape.context.DetailsLoader;

public class ContactLoader implements DetailsLoader<Contact> {
    @Override
    public Contact loadFromCSVLine(String line) {
        return Contact.fromString(line);
    }
}
