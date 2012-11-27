package com.siberhus.springbatch.item;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class WarningMessages extends Messages {
	
	public boolean add(Throwable e){
		return add(translate(e));
	}
	
	public boolean add(String fieldName, Throwable e){
		return add(fieldName+": "+translate(e));
	}
	
	protected String translate(Throwable e){
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		if(rootCause!=null){
			e = rootCause;
		}
		if(!StringUtils.isBlank(e.getMessage())){
			return e.getMessage();
		}
		return e.getClass().getSimpleName();
	}
	
	public static void main(String[] args) {
		WarningMessages m = new WarningMessages();
		m.add("Hello Exception");
		m.add("fieldName",new IllegalArgumentException(""));
		System.out.println(m);
	}
}
