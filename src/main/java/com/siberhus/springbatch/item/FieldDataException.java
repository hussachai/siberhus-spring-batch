package com.siberhus.springbatch.item;

public class FieldDataException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldDataException() {
		super();
	}
	
	public FieldDataException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FieldDataException(String message) {
		super(message);
	}

	public FieldDataException(String fieldName, String message){
		super(message);
		this.fieldName = fieldName;		
	}

	public FieldDataException(Throwable cause) {
		super(cause);
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
}
