package com.fingy.kickstarter;

public class Comment {

	private final String id;
	private final String commenterName;
	private final String commenterUrl;
	private final String timestamp;
	private final String text;

	public Comment(String id, String commenterName, String commenterUrl, String timestamp, String text) {
		this.id = id;
		this.commenterName = commenterName;
		this.commenterUrl = commenterUrl;
		this.timestamp = timestamp;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public String getCommenterName() {
		return commenterName;
	}

	public String getCommenterUrl() {
		return commenterUrl;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		Comment other = (Comment) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append("%#^");
		builder.append(commenterName);
		builder.append("%#^");
		builder.append(commenterUrl);
		builder.append("%#^");
		builder.append(timestamp);
		builder.append("%#^");
		builder.append(text);
		return builder.toString();
	}

}
