package com.fingy.mouseprice;

public class RealEstateInfo {

	private final String zip;
	private final String address;
	private final String type;
	private final String pricePaid;
	private final String date;
	private final String beds;
	private final String worth;

	public RealEstateInfo(String zip, String address, String type, String pricePaid, String date, String beds, String worth) {
		this.zip = zip;
		this.address = address;
		this.type = type;
		this.pricePaid = pricePaid;
		this.date = date;
		this.beds = beds;
		this.worth = worth;
	}

	public String getZip() {
		return zip;
	}

	public String getAddress() {
		return address;
	}

	public String getType() {
		return type;
	}

	public String getPricePaid() {
		return pricePaid;
	}

	public String getDate() {
		return date;
	}

	public String getBeds() {
		return beds;
	}

	public String getWorth() {
		return worth;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(zip);
		builder.append("§");
		builder.append(address);
		builder.append("§");
		builder.append(type);
		builder.append("§");
		builder.append(pricePaid);
		builder.append("§");
		builder.append(date);
		builder.append("§");
		builder.append(beds);
		builder.append("§");
		builder.append(worth);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		RealEstateInfo other = (RealEstateInfo) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}

	public static RealEstateInfo fromString(String csvLine) {
		String data[] = csvLine.split("§");
		return new RealEstateInfo(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
	}
}
