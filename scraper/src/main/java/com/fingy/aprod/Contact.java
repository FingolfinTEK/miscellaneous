package com.fingy.aprod;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Contact {

	public static final String FORBIDDEN_MESSAGE = "El\u00e9rted a maxim\u00e1lis limitet. Pr\u00f3b\u00e1lkozz \u00fajra egy \u00f3ra m\u00falva!";

	private final String name;
	private final String phoneNumber;

	public Contact(String name, String phoneNumber) {
		this.name = name;
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public String toString() {
		return name + ", " + phoneNumber;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(name);
		hashCodeBuilder.append(phoneNumber);
		return hashCodeBuilder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Contact)) {
			return false;
		}
		Contact other = (Contact) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(name, other.name);
		equalsBuilder.append(phoneNumber, other.phoneNumber);
		return equalsBuilder.isEquals();
	}

	public boolean isValid() {
		return !"N/A".equals(phoneNumber) && !phoneNumber.contains("limitet");
	}

}
