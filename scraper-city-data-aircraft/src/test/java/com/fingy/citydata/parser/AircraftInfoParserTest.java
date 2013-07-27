package com.fingy.citydata.parser;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.fingy.citydata.model.AircraftInfo;

public class AircraftInfoParserTest {

    private AircraftInfoParser parser = new AircraftInfoParser();

    @Test
    public void testParse() {
        String toParse = "Aircraft: PIPER PA-32R-301 (Category: Land, Seats: 7, Weight: Up to 12,499 Pounds), "
                + "Engine: LYCOMING IO-540 SER (300 HP) (Reciprocating)";
        assertThat(parser.parse(toParse)).isEqualTo(new AircraftInfo("PIPER", "PA-32R-301", "Land", "", "7", "Up to 12,499", "",
                                                            "LYCOMING", "IO-540 SER", "300 HP", "", "Reciprocating"));

        toParse = "Aircraft: NIELSEN HANS KR-1 (Category: Land, Weight: Up to 12,499 Pounds), Engine: AMA/EXPR UNKNOWN ENG (Reciprocating)";
        assertThat(parser.parse(toParse)).isEqualTo(new AircraftInfo("NIELSEN HANS", "KR-1", "Land", "", "", "Up to 12,499", "",
                                                            "AMA/EXPR", "UNKNOWN ENG", "", "", "Reciprocating"));

        toParse = "Aircraft: NIELSEN HANS KR-1 (Category: Land, Weight: Up to 12,499 Pounds), Engine: Reciprocating";
        assertThat(parser.parse(toParse)).isEqualTo(new AircraftInfo("NIELSEN HANS", "KR-1", "Land", "", "", "Up to 12,499", "", "", "",
                                                            "", "", "Reciprocating"));

        toParse = "Aircraft: , Engine: Reciprocating";
        assertThat(parser.parse(toParse)).isEqualTo(new AircraftInfo("", "", "", "", "", "", "", "", "", "", "", "Reciprocating"));
    }

}
