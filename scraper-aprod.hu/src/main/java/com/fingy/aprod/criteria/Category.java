package com.fingy.aprod.criteria;

public enum Category {

	ALL("All", "http://aprod.hu/%s/?all_categories=all"),
	PROPERTY("Property", "http://aprod.hu/ingatlan/%s/?all_categories=all"),
	VEHICLE("Vehicle", "http://aprod.hu/jarmu/%s/?all_categories=all"),
	SERVICE("Service", "http://aprod.hu/szolgaltatas/%s/?all_categories=all"),
	JOB("Job", "http://aprod.hu/allas/%s/?all_categories=all"),
	ELECTRONICS("Electronics", "http://aprod.hu/muszaki-cikk-elektronika/%s/?all_categories=all"),
	AGRICULTURE("Agriculture", "http://aprod.hu/mezogazdasag/%s/?all_categories=all"),
	HOME_GARDEN("Home & Garden", "http://aprod.hu/otthon-kert/%s/?all_categories=all"),
	FASHIN_CLOTHING("Fashion & Clothing", "http://aprod.hu/divat-ruha/%s/?all_categories=all"),
	MOTHER_BABY("Mother & Baby", "http://aprod.hu/baba-mama/%s/?all_categories=all"),
	BOOKS_MAGAZINES("Books & MAGAZINES", "http://aprod.hu/konyv-ujsag/%s/?all_categories=all"),
	SPORTS_RECREATION("Sports & Recreation", "http://aprod.hu/sport-szabadido/%s/?all_categories=all"),
	COLLECTIONS("Collections", "http://aprod.hu/gyujtemeny/%s/?all_categories=all"),
	GAMES("Games", "http://aprod.hu/jatek/%s/?all_categories=all"),
	PETS("Pets", "http://aprod.hu/haziallat/%s/?all_categories=all"),
	FILM_MUSIC("Filem & Music", "http://aprod.hu/film-zene/%s/?all_categories=all");

	private final String name;
	private final String link;

	private Category(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}

	@Override
	public String toString() {
		return name;
	}
}
