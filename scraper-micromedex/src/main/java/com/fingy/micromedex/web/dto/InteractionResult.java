package com.fingy.micromedex.web.dto;

public class InteractionResult {

	private final String drugs;
	private final String severity;
	private final String documentation;
	private final String summary;
	private final String url;

	public InteractionResult(String drugs, String severity, String documentation, String summary, String url) {
		this.drugs = drugs;
		this.severity = severity;
		this.documentation = documentation;
		this.summary = summary;
		this.url = url;
	}

	public String getDrugs() {
		return drugs;
	}

	public String getSeverity() {
		return severity;
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getSummary() {
		return summary;
	}

	public String getUrl() {
		return url;
	}

}
