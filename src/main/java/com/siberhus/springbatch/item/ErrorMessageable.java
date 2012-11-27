package com.siberhus.springbatch.item;

public interface ErrorMessageable {

	
	/*
	 * Avoid get* naming convention because some ORM framework
	 * will persist this property.
	 */
	public ErrorMessages errorMessages();
}
