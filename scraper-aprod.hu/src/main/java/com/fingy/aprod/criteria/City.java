package com.fingy.aprod.criteria;

public enum City {
	BACS_KISKUN("Bacs-Kiskun","bacs-kiskun"),
	BARANYA("Baranya","baranya"),
	BEKES("Bekes","bekes"),
	BORSOD_ABAUJ_ZEMPLEN("Borsod-Abauj-Zemplen","borsod-abauj-zemplen"),
	BUDAPEST("Budapest","budapest"),
	CSONGRAD("Csongrad","csongrad"),
	DEBRECEN("Debrecen","debrecen"),
	FEJER("Fejer","fejer"),
	GYOR("Gyor","gyor"),
	GYOR_MOSON_SOPRON("Gyor-Moson-Sopron","gyor-moson-sopron"),
	HAJDU_BIHAR("Hajdu-Bihar","hajdu-bihar"),
	HEVES("Heves","heves"),
	JASZ_NAGYKUN_SZOLNOK("Jasz-Nagykun-Szolnok","jasz-nagykun-szolnok"),
	KECSKEMET("Kecskemet","kecskemet"),
	KOMAROM_ESZTERGOM("Komarom-Esztergom","komarom-esztergom"),
	MISKOLC("Miskolc","miskolc"),
	NOGRAD("Nograd","nograd"),
	PECS("Pecs","pecs"),
	PEST("Pest","pest"),
	SOMOGY("Somogy","somogy"),
	SZABOLCS_SZATMAR_BEREG("Szabolcs-Szatmar-Bereg","szabolcs-szatmar-bereg"),
	SZEGED("Szeged","szeged"),
	SZEKESFEHERVAR("Szekesfehervar","szekesfehervar"),
	TOLNA("Tolna","tolna"),
	VAS("Vas","vas"),
	VESZPREM("Veszprem","veszprem"),
	ZALA("Zala","zala");

	private final String displayName;
	private final String urlName;

	private City(String displayName, String urlName) {
		this.displayName = displayName;
		this.urlName = urlName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUrlName() {
		return urlName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
