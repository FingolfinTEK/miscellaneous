package com.fingy.micromedex.web.dto;

public class DrugResult {

	private final String name;
	private final String url;

	public DrugResult(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

}
