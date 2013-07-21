package com.fingy.citydata.parser;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.fingy.citydata.model.RegistrantInfo;

public class RegistrantInfoParserTest {

    private RegistrantInfoParser parser = new RegistrantInfoParser();

    @Test
    public void testParse() {
        String toParse = "Registrant (Corporation): Cochran & Jones LLC, Po Box 1576, Albertville, AL 35950";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrantInfo("Cochran & Jones LLC", "", "Po Box 1576", "Albertville", "AL",
                                                            "35950"));

        toParse = "Registrant (Individual): Ross Adams, 189 Rock Springs Rd, Albertville, AL 35950";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrantInfo("", "Ross Adams", "189 Rock Springs Rd", "Albertville", "AL",
                                                            "35950"));

        toParse = "Registrant (Partnership): William T Tomlinson, 5002 County Road 3, Albertville, AL 35951, Other Owners: Coy L Murray";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrantInfo("", "William T Tomlinson", "5002 County Road 3", "Albertville",
                                                            "AL", "35951"));

        toParse = "Registrant (Corporation): P And B Flying Svc Inc, , Pensacola, FL 32501";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrantInfo("P And B Flying Svc Inc", "", "", "Pensacola", "FL", "32501"));

        toParse = "Registrant (): P And B Flying Svc Inc, , Pensacola, FL 32501";
        assertThat(parser.parse(toParse)).isEqualTo(new RegistrantInfo("P And B Flying Svc Inc", "", "", "Pensacola", "FL", "32501"));
    }

}
