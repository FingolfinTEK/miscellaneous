package com.fingy.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fingy.scrape.util.HttpClientParserUtil;

public class IOHelper {

	public static byte[] readContent(InputStream input, Class<? extends IOException> ignored) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	
		try {
			int n = 0;
			byte[] buffer = new byte[HttpClientParserUtil.DEFAULT_BUFFER_SIZE];
	
			while (HttpClientParserUtil.EOF != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} catch (IOException ex) {
			if (!ignored.isInstance(ignored))
				throw ex;
		}
	
		return output.toByteArray();
	}

}
