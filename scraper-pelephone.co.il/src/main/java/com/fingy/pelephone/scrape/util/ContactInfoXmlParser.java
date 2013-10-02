package com.fingy.pelephone.scrape.util;

import com.fingy.pelephone.ContactInfo;
import com.fingy.scrape.util.JsoupParserUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;

public class ContactInfoXmlParser {

    public List<ContactInfo> parse(String xml) {
        List<ContactInfo> contacts = new LinkedList<>();
        Document data = Jsoup.parse(xml, "", Parser.xmlParser());
        for (Element reshet : data.select("reshet")) {
            String reshetCode = JsoupParserUtil.getTagTextFromCssQuery(reshet, "code");
            for (Element contactTag : reshet.select("p")) {
                ContactInfo contactInfo = parseContact(reshetCode, contactTag);
                contacts.add(contactInfo);
            }
        }
        return contacts;
    }

    private ContactInfo parseContact(String reshetCode, Element contactTag) {
        String ordinalId = JsoupParserUtil.getTagTextFromCssQuery(contactTag, "ordinalnum");
        String name = JsoupParserUtil.getTagTextFromCssQuery(contactTag, "privatename");
        String address = JsoupParserUtil.getTagTextFromCssQuery(contactTag, "address");
        return new ContactInfo(reshetCode, ordinalId, name, address, "");
    }
}
