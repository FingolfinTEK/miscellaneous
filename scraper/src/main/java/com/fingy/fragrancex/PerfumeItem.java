package com.fingy.fragrancex;

public class PerfumeItem {
	private String id;
	private String name;
	private String description;
	private String price;
	private String imageUrl;

	private int rowInWorksheet;

	public PerfumeItem(String id, String name, String description, String price, String imageUrl, int rowInWorksheet) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.rowInWorksheet = rowInWorksheet;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getRowInWorksheet() {
		return rowInWorksheet;
	}

	public void setRowInWorksheet(int rowInWorksheet) {
		this.rowInWorksheet = rowInWorksheet;
	}

	@Override
	public String toString() {
		return "PerfumeItem [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price
				+ ", imageUrl=" + imageUrl + ", rowInWorksheet=" + rowInWorksheet + "]";
	}

}
