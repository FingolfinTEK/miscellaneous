package com.fingy.pelephone.scrape;

import com.fingy.pelephone.ContactInfo;
import com.fingy.pelephone.scrape.util.ContactInfoXmlParser;
import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

public class ContactInfoXmlParserTest {
    @Test
    public void testParse() throws Exception {
        URL filePath = getClass().getClassLoader().getResource("data.xml");
        String data = FileUtils.readFileToString(new File(filePath.getFile()), "UTF-8");
        List<ContactInfo> contacts = new ContactInfoXmlParser().parse(data);
        Assertions.assertThat(contacts).hasSize(74);
    }
}
