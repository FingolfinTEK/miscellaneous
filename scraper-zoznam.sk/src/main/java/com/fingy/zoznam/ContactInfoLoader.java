package com.fingy.zoznam;

public class ContactInfoLoader implements DetailsLoader<ContactInfo> {

    @Override
    public ContactInfo loadFromCSVLine(final String line) {
        String data[] = line.split("§");
        return new ContactInfo(data[0], data[1], data[2]);
    }

}
