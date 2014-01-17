package com.fingy.proz;

import com.fingy.scrape.context.ScrapeDetails;

public class ContactInfo extends ScrapeDetails {

    private String userName;
    private String name;
    private String companyName;
    private String website;
    private String address;
    private String country;
    private String phoneNumber;
    private String email;

    public ContactInfo(final String userName, final String companyName, final String name, final String website, final String address, final String country,
                       final String phoneNumber, final String email) {
        this.userName = userName;
        this.companyName = companyName;
        this.name = name;
        this.website = website;
        this.address = address;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    @Override
    public String toString() {
        return userName + "§" + companyName + "§" + name + "§" + website + "§" + address + "§" + country + "§" + phoneNumber + "§" + email;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ContactInfo that = (ContactInfo) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null) return false;
        if (website != null ? !website.equals(that.website) : that.website != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
