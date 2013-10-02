package com.fingy.pelephone.scrape.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonHelper {
    public static String parseJson(String jsonData) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonData);
        JsonObject object = element.getAsJsonObject();
        return object.get("d").getAsString();
    }
}