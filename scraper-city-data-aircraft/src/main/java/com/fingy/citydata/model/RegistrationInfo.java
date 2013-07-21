package com.fingy.citydata.model;

public class RegistrationInfo {

    private final String nNumber;
    private final String serialNumber;
    private final String manufactured;
    private final String airworthinessDate;

    public RegistrationInfo(String nNumber, String serialNumber, String manufactured, String airworthinessDate) {
        this.nNumber = nNumber;
        this.serialNumber = serialNumber;
        this.manufactured = manufactured;
        this.airworthinessDate = airworthinessDate;
    }

    public String getnNumber() {
        return nNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getManufactured() {
        return manufactured;
    }

    public String getAirworthinessDate() {
        return airworthinessDate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(nNumber);
        builder.append("§");
        builder.append(serialNumber);
        builder.append("§");
        builder.append(manufactured);
        builder.append("§");
        builder.append(airworthinessDate);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((airworthinessDate == null) ? 0 : airworthinessDate.hashCode());
        result = prime * result + ((manufactured == null) ? 0 : manufactured.hashCode());
        result = prime * result + ((nNumber == null) ? 0 : nNumber.hashCode());
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegistrationInfo other = (RegistrationInfo) obj;
        if (airworthinessDate == null) {
            if (other.airworthinessDate != null)
                return false;
        } else if (!airworthinessDate.equals(other.airworthinessDate))
            return false;
        if (manufactured == null) {
            if (other.manufactured != null)
                return false;
        } else if (!manufactured.equals(other.manufactured))
            return false;
        if (nNumber == null) {
            if (other.nNumber != null)
                return false;
        } else if (!nNumber.equals(other.nNumber))
            return false;
        if (serialNumber == null) {
            if (other.serialNumber != null)
                return false;
        } else if (!serialNumber.equals(other.serialNumber))
            return false;
        return true;
    }

}
