package com.fingy.proz;

import com.fingy.scrape.context.DetailsLoader;

public class ContactInfoLoader implements DetailsLoader<ContactInfo> {

    @Override
    public ContactInfo loadFromCSVLine(final String line) {
        final String data[] = line.split("§");
        return new ContactInfo(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
    }

}
