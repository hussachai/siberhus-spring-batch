package com.siberhus.springbatch.item.file.transform;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * 
 * @author hussachai
 *
 */
public class DefaultFieldSet implements FieldSet{
	
	private String values[];
	
	private List<String> names;
	
	public DefaultFieldSet(){
		
	}
	public DefaultFieldSet(String values[]){
		this.values = values == null ? null : (String[]) values.clone();
	}
	
	public DefaultFieldSet(String values[], String names[]){
		Assert.notNull(values);
		Assert.notNull(names);
		if (values.length != names.length) {
			throw new IllegalArgumentException("Field names must be same length as values: names="
					+ Arrays.asList(names) + ", values=" + Arrays.asList(values));
		}
		this.values = (String[]) values.clone();
		this.names = Arrays.asList(names);
	}
	
	@Override
	public String[] getNames() {
		if (names == null) {
			throw new IllegalStateException("Field names are not known");
		}
		return names.toArray(new String[names.size()]);
	}
	
	@Override
	public boolean hasNames() {
		return names != null;
	}
	
	@Override
	public String[] getValues() {
		return values.clone();
	}
	
	@Override
	public int getFieldCount() {
		return values.length;
	}
	
	@Override
	public String readString(int index) {
		return readAndTrim(index);
	}
	
	@Override
	public String readString(int index, String defaultValue){
		String value = readAndTrim(index);
		if(value==null){
			return defaultValue;
		}
		return value;
	}
	
	@Override
	public String readString(String name) {
		return readString(indexOf(name));
	}
	
	@Override
	public String readString(String name, String defaultValue) {
		return readString(indexOf(name), defaultValue);
	}
	
	@Override
	public String readRawString(int index) {
		return values[index];
	}
	
	@Override
	public String readRawString(String name) {
		return readRawString(indexOf(name));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <D>D read(Class<D> clazz, int index){
		String value = readString(index);
		if(value==null){
			return null;
		}
		return (D)ConvertUtils.convert(value, clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <D>D read(Class<D> clazz, int index, D defaultValue){
		String value = readString(index);
		if(value==null){
			return defaultValue;
		}
		return (D)ConvertUtils.convert(value, clazz);
	}
	
	@Override
	public <D>D read(Class<D> clazz, String name){
		return read(clazz, indexOf(name));
	}
	
	@Override
	public <D>D read(Class<D> clazz, String name, D defaultValue){
		return read(clazz, indexOf(name), defaultValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <D>D[] readArray(Class<D> clazz, int index, String separator){
		String subValues[] = StringUtils.split(readString(index), separator);
		if(subValues==null){
			return null;
		}
		return (D[])ConvertUtils.convert(subValues, clazz);
	}
	
	@Override
	public <D>D[] readArray(Class<D> clazz, String name, String separator){
		return readArray(clazz, indexOf(name), separator);
	}
	
	protected String readAndTrim(int index) {
		return StringUtils.trimToNull(values[index]);		
	}
	
	protected int indexOf(String name) {
		if (names == null) {
			throw new IllegalArgumentException("Cannot access columns by name without meta data");
		}
		int index = names.indexOf(name);
		if (index >= 0) {
			return index;
		}
		throw new IllegalArgumentException("Cannot access column [" + name + "] from " + names);
	}
}
