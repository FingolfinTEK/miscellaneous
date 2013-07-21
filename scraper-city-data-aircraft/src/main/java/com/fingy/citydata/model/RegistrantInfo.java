package com.fingy.citydata.model;

public class RegistrantInfo {

    private final String companyName;
    private final String ownerName;
    private final String address;
    private final String city;
    private final String state;
    private final String zipCode;

    public RegistrantInfo(String companyName, String ownerName, String address, String city, String state, String zipCode) {
        this.companyName = companyName;
        this.ownerName = ownerName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public RegistrantInfo() {
        this("", "", "", "", "", "");
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((companyName == null) ? 0 : companyName.hashCode());
        result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
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
        RegistrantInfo other = (RegistrantInfo) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (companyName == null) {
            if (other.companyName != null)
                return false;
        } else if (!companyName.equals(other.companyName))
            return false;
        if (ownerName == null) {
            if (other.ownerName != null)
                return false;
        } else if (!ownerName.equals(other.ownerName))
            return false;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        if (zipCode == null) {
            if (other.zipCode != null)
                return false;
        } else if (!zipCode.equals(other.zipCode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(companyName);
        builder.append("§");
        builder.append(ownerName);
        builder.append("§");
        builder.append(address);
        builder.append("§");
        builder.append(city);
        builder.append("§");
        builder.append(state);
        builder.append("§");
        builder.append(zipCode);
        return builder.toString();
    }

}
