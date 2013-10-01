package com.fingy.fragrancex.scrape;

public class FragrancexTask {

	private final int row;
	private final String id;
	private final String sizeOrType;

	public FragrancexTask(int row, String id, String sizeOrType) {
		this.row = row;
		this.id = id;
		this.sizeOrType = sizeOrType;
	}

	public int getRow() {
		return row;
	}

	public String getId() {
		return id;
	}

	public String getSizeOrType() {
		return sizeOrType;
	}

	@Override
	public String toString() {
		return "FragrancexTask [row=" + row + ", id=" + id + ", sizeOrType=" + sizeOrType + "]";
	}

}
