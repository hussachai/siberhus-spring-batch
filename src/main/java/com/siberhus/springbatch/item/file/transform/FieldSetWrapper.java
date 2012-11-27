package com.siberhus.springbatch.item.file.transform;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author hussachai
 *
 */
public class FieldSetWrapper implements FieldSet{
	
	private List<String> names;
	
	private org.springframework.batch.item.file.transform.FieldSet delegate;
	
	public FieldSetWrapper(org.springframework.batch.item.file.transform.FieldSet fieldSet){
		this.delegate = fieldSet;
		if(delegate.getNames()!=null){
			this.names = Arrays.asList(delegate.getNames());
		}
	}
	
	@Override
	public String[] getNames() {
		return delegate.getNames();
	}
	
	@Override
	public boolean hasNames() {
		return delegate.hasNames();
	}
	
	@Override
	public String[] getValues() {
		return delegate.getValues();
	}
	
	@Override
	public int getFieldCount() {
		return delegate.getFieldCount();
	}
	
	@Override
	public String readString(int index) {
		if(index<0){
			return null;
		}
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
		if(index<0){
			return null;
		}
		return delegate.readRawString(index);
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
		return StringUtils.trimToNull(readRawString(index));		
	}
	
	protected int indexOf(String name) {
		if (delegate.getNames() == null) {
			throw new IllegalArgumentException("Cannot access columns by name without meta data");
		}
		int index = names.indexOf(name);
		return index;		
	}
	
	public static void main(String[] args) {
		ConvertUtils.convert("anc", java.util.Date.class);
	}
}
