package com.fingy.citydata.model;

public class AircraftRegistrationInfo {

    private final AircraftInfo aircraftInfo;
    private final RegistrationInfo registrationInfo;
    private final RegistrantInfo registrantInfo;

    public AircraftRegistrationInfo(AircraftInfo aircraftInfo, RegistrationInfo registrationInfo, RegistrantInfo registrantInfo) {
        this.aircraftInfo = aircraftInfo;
        this.registrationInfo = registrationInfo;
        this.registrantInfo = registrantInfo;
    }

    public AircraftInfo getAircraftInfo() {
        return aircraftInfo;
    }

    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo;
    }

    public RegistrantInfo getRegistrantInfo() {
        return registrantInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(aircraftInfo);
        builder.append("§");
        builder.append(registrationInfo);
        builder.append("§");
        builder.append(registrantInfo);
        return builder.toString();
    }



}
