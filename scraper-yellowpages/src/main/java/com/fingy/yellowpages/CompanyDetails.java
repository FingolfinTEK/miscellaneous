package com.fingy.yellowpages;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CompanyDetails {

	private final String name;
	private final String address;
	private final String phone;
	private final String website;
	private final String email;

	public CompanyDetails(String name, String address, String phone, String website, String email) {
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.website = website;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public String getWebsite() {
		return website;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("§");
		builder.append(address);
		builder.append("§");
		builder.append(phone);
		builder.append("§");
		builder.append(website);
		builder.append("§");
		builder.append(email);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(name);
		builder.append(address);
		builder.append(phone);
		builder.append(website);
		builder.append(email);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CompanyDetails))
			return false;

		CompanyDetails other = (CompanyDetails) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(name, other.name);
		builder.append(address, other.address);
		builder.append(phone, other.phone);
		builder.append(website, other.website);
		builder.append(email, other.email);
		return builder.isEquals();
	}

}
