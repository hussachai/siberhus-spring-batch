package com.siberhus.springbatch.format;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

public class DateTypeFormat implements TypeFormat<Date>{
	
	private String pattern;
	
	private Locale locale;
	
	private TimeZone timeZone;
	
	@Override
	public String format(Date value) {
		if(locale!=null && timeZone!=null){
			return FastDateFormat.getInstance(pattern, timeZone, locale).format(value);
		}else if(locale!=null){
			return FastDateFormat.getInstance(pattern, locale).format(value);
		}else if(timeZone!=null){
			return FastDateFormat.getInstance(pattern, timeZone).format(value);
		}
		return FastDateFormat.getInstance(pattern).format(value);
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
}
