package com.fingy.aprod;

public enum Category {

	ALL("All", "http://aprod.hu/budapest/?all_categories=all"),
	PROPERTY("Property", "http://aprod.hu/ingatlan/budapest/?all_categories=all"),
	VEHICLE("Vehicle", "http://aprod.hu/jarmu/budapest/?all_categories=all"),
	SERVICE("Service", "http://aprod.hu/szolgaltatas/budapest/?all_categories=all"),
	JOB("Job", "http://aprod.hu/allas/budapest/?all_categories=all"),
	ELECTRONICS("Electronics", "http://aprod.hu/muszaki-cikk-elektronika/budapest/?all_categories=all"),
	AGRICULTURE("Agriculture", "http://aprod.hu/mezogazdasag/budapest/?all_categories=all"),
	HOME_GARDEN("Home & Garden", "http://aprod.hu/otthon-kert/budapest/?all_categories=all"),
	FASHIN_CLOTHING("Fashion & Clothing", "http://aprod.hu/divat-ruha/budapest/?all_categories=all"),
	MOTHER_BABY("Mother & Baby", "http://aprod.hu/baba-mama/budapest/?all_categories=all"),
	BOOKS_MAGAZINES("Books & MAGAZINES", "http://aprod.hu/konyv-ujsag/budapest/?all_categories=all"),
	SPORTS_RECREATION("Sports & Recreation", "http://aprod.hu/sport-szabadido/budapest/?all_categories=all"),
	COLLECTIONS("Collections", "http://aprod.hu/gyujtemeny/budapest/?all_categories=all"),
	GAMES("Games", "http://aprod.hu/jatek/budapest/?all_categories=all"),
	PETS("Pets", "http://aprod.hu/haziallat/budapest/?all_categories=all"),
	FILM_MUSIC("Filem & Music", "http://aprod.hu/film-zene/budapest/?all_categories=all");

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
