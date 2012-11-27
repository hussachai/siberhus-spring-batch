package com.siberhus.springbatch.item.file.mapping;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.AbstractLineTokenizer;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class FieldNameAwareLineMapper <T> extends DefaultLineMapper<T> {
	
	private AbstractLineTokenizer tokenizer;
	
	private NumberFormat numberFormat;
	
	private DateFormat dateFormat;
	
	public void setNames(String line){
		tokenizer.setNames(tokenizer.tokenize(line).getValues());		
	}
	
	@Override
	public void setLineTokenizer(LineTokenizer tokenizer) {
		super.setLineTokenizer(tokenizer);
		if(tokenizer instanceof AbstractLineTokenizer){
			this.tokenizer = (AbstractLineTokenizer)tokenizer;
		}else{
			throw new IllegalArgumentException("lineMapper must be a class or subclass of "
					+FieldNameAwareLineMapper.class.getName());
		}
	}
	
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		DefaultFieldSetFactory fsf = new DefaultFieldSetFactory();		
		if(dateFormat!=null){
			fsf.setDateFormat(dateFormat);
		}
		if(numberFormat!=null){
			fsf.setNumberFormat(numberFormat);
		}
		tokenizer.setFieldSetFactory(fsf);
	}
	
	public void setDefaultNumberFormatString(String numberFormat) {
		this.numberFormat = new DecimalFormat(numberFormat);
	}
	
	public void setDefaultNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}
	
	public void setDefaultDateFormatString(String dateFormat) {
		this.dateFormat = new SimpleDateFormat(dateFormat);		
	}
	
	public void setDefaultDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
}
