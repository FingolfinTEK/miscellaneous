package com.fingy.micromedex.util;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtil {

	public static <T> T readStringValueAsType(String value, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(value, valueType);
	}

}
