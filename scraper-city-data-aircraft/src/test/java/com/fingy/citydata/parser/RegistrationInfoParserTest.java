package com.fingy.citydata.parser;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.fingy.citydata.model.RegistrationInfo;

public class RegistrationInfoParserTest {

    private RegistrationInfoParser parser = new RegistrationInfoParser();

    @Test
    public void testParse() {
        String toParse = "N-Number: 115CS , Serial Number: 32-46177, Year manufactured: 2000, Airworthiness Date: 10/25/2000";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrationInfo("115CS", "32-46177", "2000", "10/25/2000"));

        toParse = "N-Number: 717EG , Serial Number: 0704";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrationInfo("717EG", "0704", "", ""));
    }

}
