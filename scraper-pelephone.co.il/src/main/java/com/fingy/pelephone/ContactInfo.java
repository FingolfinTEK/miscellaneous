package com.fingy.pelephone;

import org.apache.commons.lang3.StringUtils;

public class ContactInfo {
    private String reshet;
    private String ordinalId;
    private String name;
    private String address;
    private String telephoneNumber;

    public ContactInfo(String reshet, String ordinalId, String name, String address, String telephoneNumber) {
        this.reshet = reshet;
        this.ordinalId = ordinalId;
        this.name = name;
        this.address = address;
        this.telephoneNumber = telephoneNumber;
    }

    public String getReshet() {
        return reshet;
    }

    public void setReshet(String reshet) {
        this.reshet = reshet;
    }

    public String getOrdinalId() {
        return ordinalId;
    }

    public void setOrdinalId(String ordinalId) {
        this.ordinalId = ordinalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getOrdinalIdForAjax() {
        return ordinalId.substring(reshet.length(), ordinalId.length());
    }

    public boolean hasValidPhoneNumber() {
        return StringUtils.isNotBlank(telephoneNumber) && !"cap".equals(telephoneNumber);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("§");
        builder.append(address);
        builder.append("§");
        builder.append(telephoneNumber);
        return builder.toString();
    }
}
