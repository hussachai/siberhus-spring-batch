package com.siberhus.springbatch.item.file;

import java.io.IOException;

public class UnknownFileTypeException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownFileTypeException() {
		super();
	}

	public UnknownFileTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownFileTypeException(String message) {
		super(message);
	}

	public UnknownFileTypeException(Throwable cause) {
		super(cause);
	}

	
	
}
