package com.fingy.citydata.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fingy.citydata.model.RegistrantInfo;

public class RegistrantInfoParser implements Parser<RegistrantInfo> {

    private static final String INFO_REGEX = "Registrant \\((.*)\\): ([^,]*), ([^,]*), ([^,]*), ([^ ]*) ([^,]*)(, .*)?";
    private static final Pattern INFO_PATTERN = Pattern.compile(INFO_REGEX);

    @Override
    public RegistrantInfo parse(String stringToParse) {

        Matcher matcher = INFO_PATTERN.matcher(stringToParse);
        if (matcher.matches()) {
            final String registrantType = getGroup(matcher, 1);
            final String companyName = isCorporation(registrantType) ? getGroup(matcher, 2) : "";
            final String ownerName = isCorporation(registrantType) ? "" : getGroup(matcher, 2);
            final String address = getGroup(matcher, 3);
            final String city = getGroup(matcher, 4);
            final String state = getGroup(matcher, 5);
            final String zipCode = getGroup(matcher, 6);

            return new RegistrantInfo(companyName, ownerName, address, city, state, zipCode);
        }

        return new RegistrantInfo();
    }

    private boolean isCorporation(final String registrantType) {
        return StringUtils.isBlank(registrantType) || "Corporation".equals(registrantType);
    }

    private String getGroup(Matcher matcher, int group) {
        return StringUtils.stripToEmpty(matcher.group(group));
    }

}
