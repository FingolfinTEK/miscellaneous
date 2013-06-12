package com.fingy.scrape.exception;


public class ScrapeException extends RuntimeException {

	private static final long serialVersionUID = 4312264649767630356L;

	public ScrapeException() {
	}

	public ScrapeException(String message) {
		super(message);
	}
	
	public ScrapeException(Throwable cause) {
		super(cause);
	}

	public ScrapeException(String message, Throwable cause) {
		super(message, cause);
	}

}
