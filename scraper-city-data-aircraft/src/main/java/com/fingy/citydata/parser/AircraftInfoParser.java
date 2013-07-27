package com.fingy.citydata.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fingy.citydata.model.AircraftInfo;

public class AircraftInfoParser implements Parser<AircraftInfo> {

    private static final int MAKE_AND_MDEL_GROUP = 2;
    private static final int CATEGORY_GROUP = 4;
    private static final int NUMBER_OF_ENGINES_GROUP = 6;
    private static final int NUMBER_OF_SEATS_GROUP = 8;
    private static final int WEIGHT_GROUP = 10;
    private static final int SPEED_GROUP = 12;
    private static final int ENGINE_INFO_GROUP = 14;
    private static final int ENGINE_POWER_AND_TYPE_GROUP = 15;
    private static final int ENGINE_POWER_GROUP = 16;
    private static final int ENGINE_TYPE_GROUP = 17;

    private static final String MODEL_INFO = "(Category: (\\w+))?(, Engines: (\\d+))?(, Seats: (\\d+))?(, Weight: (.+) Pounds)(, Speed: (.+))?";
    private static final String ENGINE_INFO = "Engine: ?(([^\\(]+)(\\(?([^\\)]+)?\\)? ?\\((.*)\\))?)?";
    private static final String INFO_REGEX = "Aircraft: ((.*) \\(" + MODEL_INFO + "\\))?, " + ENGINE_INFO;
    private static final Pattern INFO_PATTERN = Pattern.compile(INFO_REGEX);

    private static final String AIRCRAFT_MAKERS_TXT = "aircraft-makers.txt";
    private static final String ENGINE_MAKERS_TXT = "engine-makers.txt";
    private static final Collection<String> aircraftMakers = loadAircraftMakers();
    private static final Collection<String> engineMakers = loadEngineMakers();

    private static Collection<String> loadAircraftMakers() {
        Collection<String> makers = new ArrayList<>();
        InputStream makersInput = AircraftInfoParser.class.getClassLoader().getResourceAsStream(AIRCRAFT_MAKERS_TXT);

        try {
            makers.addAll(IOUtils.readLines(makersInput));
        } catch (IOException ignored) {
        }

        return makers;
    }

    private static Collection<String> loadEngineMakers() {
        Collection<String> makers = new ArrayList<>();
        InputStream makersInput = AircraftInfoParser.class.getClassLoader().getResourceAsStream(ENGINE_MAKERS_TXT);

        try {
            makers.addAll(IOUtils.readLines(makersInput));
        } catch (IOException ignored) {
        }

        return makers;
    }

    @Override
    public AircraftInfo parse(String stringToParse) {
        String cleanedString = cleanString(stringToParse);

        Matcher matcher = INFO_PATTERN.matcher(cleanedString);
        if (matcher.matches()) {
            final String makeAndModel = parseMakeAndModel(matcher);
            final String make = extractMake(makeAndModel);
            final String model = extractModel(make, makeAndModel);
            final String category = parseCategory(matcher);
            final String numberOfEngines = parseNumberOfEngines(matcher);
            final String numberOfSeats = parseNumberOfSeats(matcher);
            final String weight = parseWeight(matcher);
            final String speed = parseSpeed(matcher);
            final String engineManufacturerAndModel = parseEngineMakeAndModel(matcher);
            final String engineMake = extractEngineMake(engineManufacturerAndModel);
            final String engineModel = extractEngineModel(engineMake, engineManufacturerAndModel);
            final String typeOfEngine = parseTypeOfEngine(matcher);
            final String reciprocatingPower = parseReciprocatingPower(matcher, typeOfEngine);
            final String turboFanPower = parseTurboFanPower(matcher, typeOfEngine);

            return new AircraftInfo(make, model, category, numberOfEngines, numberOfSeats, weight, speed, engineMake, engineModel,
                    reciprocatingPower, turboFanPower, typeOfEngine);
        }

        return new AircraftInfo();
    }

    private String cleanString(String stringToParse) {
        return stringToParse.replace("&amp;", "&");
    }

    private String parseMakeAndModel(Matcher matcher) {
        return getGroup(matcher, MAKE_AND_MDEL_GROUP);
    }

    private String extractMake(String makeAndModel) {
        for (String maker : aircraftMakers) {
            if (makeAndModel.startsWith(maker))
                return maker;
        }

        return "";
    }

    private String extractModel(String make, String makeAndModel) {
        return makeAndModel.replace(make, "").trim();
    }

    private String parseCategory(Matcher matcher) {
        return getGroup(matcher, CATEGORY_GROUP);
    }

    private String getGroup(Matcher matcher, int group) {
        return StringUtils.stripToEmpty(matcher.group(group));
    }

    private String parseNumberOfEngines(Matcher matcher) {
        return getGroup(matcher, NUMBER_OF_ENGINES_GROUP);
    }

    private String parseNumberOfSeats(Matcher matcher) {
        return getGroup(matcher, NUMBER_OF_SEATS_GROUP);
    }

    private String parseWeight(Matcher matcher) {
        return getGroup(matcher, WEIGHT_GROUP);
    }

    private String parseSpeed(Matcher matcher) {
        return getGroup(matcher, SPEED_GROUP);
    }

    private String parseEngineMakeAndModel(Matcher matcher) {
        return StringUtils.isBlank(matcher.group(ENGINE_POWER_AND_TYPE_GROUP)) ? "" : getGroup(matcher, ENGINE_INFO_GROUP);
    }

    private String extractEngineMake(String engineManufacturerAndModel) {
        for (String maker : engineMakers) {
            if (engineManufacturerAndModel.startsWith(maker))
                return maker;
        }

        return "";
    }

    private String extractEngineModel(String engineMake, String engineManufacturerAndModel) {
        return engineManufacturerAndModel.replace(engineMake, "").trim();
    }

    private String parseReciprocatingPower(Matcher matcher, final String typeOfEngine) {
        return isReciprocating(typeOfEngine) ? getGroup(matcher, ENGINE_POWER_GROUP) : "";
    }

    private String parseTurboFanPower(Matcher matcher, final String typeOfEngine) {
        return isReciprocating(typeOfEngine) ? "" : getGroup(matcher, ENGINE_POWER_GROUP);
    }

    private boolean isReciprocating(final String typeOfEngine) {
        return "Reciprocating".equals(typeOfEngine);
    }

    private String parseTypeOfEngine(Matcher matcher) {
        return StringUtils.isBlank(matcher.group(ENGINE_POWER_AND_TYPE_GROUP)) ? getGroup(matcher, ENGINE_INFO_GROUP)
                : getGroup(matcher, ENGINE_TYPE_GROUP);
    }

}
