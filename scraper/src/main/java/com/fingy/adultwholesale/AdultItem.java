package com.fingy.adultwholesale;

public class AdultItem {

	private String id;
	private String title;
	private String category;
	private String price;
	private String stockStatus;
	private String description;
	private String productUrl;
	private String imageUrl;

	public AdultItem() {
	}

	public AdultItem(String id, String title, String category, String price, String stockStatus, String description,
			String productUrl, String imageUrl) {
		super();
		this.id = id;
		this.title = title;
		this.category = category;
		this.price = price;
		this.stockStatus = stockStatus;
		this.description = description;
		this.productUrl = productUrl;
		this.imageUrl = imageUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getStockStatus() {
		return stockStatus;
	}

	public void setStockStatus(String stockStatus) {
		this.stockStatus = stockStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public String toString() {
		return "AdultItem [id=" + id + ", title=" + title + ", category=" + category + ", price=" + price
				+ ", stockStatus=" + stockStatus + ", description=" + description + ", productUrl=" + productUrl
				+ ", imageUrl=" + imageUrl + "]";
	}
}
