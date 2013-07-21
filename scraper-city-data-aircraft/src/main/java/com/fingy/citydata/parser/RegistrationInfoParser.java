package com.fingy.citydata.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fingy.citydata.model.RegistrationInfo;

public class RegistrationInfoParser implements Parser<RegistrationInfo> {

    private static final String INFO_REGEX = "N-Number: (.+) , Serial Number: ([^,]+)(, Year manufactured: (\\d+))?(, Airworthiness Date: (.+))?";
    private static final Pattern INFO_PATTERN = Pattern.compile(INFO_REGEX);

    @Override
    public RegistrationInfo parse(String stringToParse) {
        Matcher matcher = INFO_PATTERN.matcher(stringToParse);
        if (matcher.matches()) {
            final String nNumber = matcher.group(1);
            final String serialNumber = matcher.group(2);
            final String manufactured = StringUtils.stripToEmpty(matcher.group(4));
            final String airworthinessDate = StringUtils.stripToEmpty(matcher.group(6));

            return new RegistrationInfo(nNumber, serialNumber, manufactured, airworthinessDate);
        }

        return null;
    }

}
