package com.siberhus.springbatch.item.file.transform;

/**
 * 
 * @author hussachai
 *
 */
public interface FieldSet {

	int getFieldCount();
	
	String[] getNames();

	boolean hasNames();

	String[] getValues();
	
	String readString(int index);

	String readString(int index, String defaultValue);

	String readString(String name);

	String readString(String name, String defaultValue);
	
	String readRawString(int index);

	String readRawString(String name);
	
	<D> D read(Class<D> clazz, int index);
	
	<D> D read(Class<D> clazz, int index, D defaultValue);

	<D> D read(Class<D> clazz, String name);

	<D> D read(Class<D> clazz, String name, D defaultValue);
	
	<D>D[] readArray(Class<D> clazz, int index, String separator);
	
	<D>D[] readArray(Class<D> clazz, String name, String separator);
	
}
