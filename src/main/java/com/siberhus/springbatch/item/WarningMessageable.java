package com.siberhus.springbatch.item;

public interface WarningMessageable {
	
	/*
	 * Avoid get* naming convention because some ORM framework
	 * will persist this property.
	 */
	public WarningMessages warningMessages();
}
