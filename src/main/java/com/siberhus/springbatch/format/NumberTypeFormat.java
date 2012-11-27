package com.siberhus.springbatch.format;

import java.text.DecimalFormat;

public class NumberTypeFormat implements TypeFormat<Number>{
	
	ThreadLocal<DecimalFormat> formatCache = new ThreadLocal<DecimalFormat>();
	
	private String pattern;
	
	@Override
	public String format(Number value) {
		DecimalFormat numberFormat = formatCache.get();
		if(numberFormat==null){
			numberFormat = new DecimalFormat(pattern);
			formatCache.set(numberFormat);
		}
		
		return numberFormat.format(value);
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
