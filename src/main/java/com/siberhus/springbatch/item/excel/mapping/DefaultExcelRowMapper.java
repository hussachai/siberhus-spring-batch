package com.siberhus.springbatch.item.excel.mapping;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.ObjectUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.siberhus.springbatch.item.excel.ExcelRowMapper;
import com.siberhus.springbatch.item.excel.transform.DefaultExcelFieldSetFactory;
import com.siberhus.springbatch.item.excel.transform.ExcelFieldSetFactory;

public class DefaultExcelRowMapper<T> implements ExcelRowMapper<T>, InitializingBean {
	
	private FieldSetMapper<T> fieldSetMapper;
	
	//Because excel file does not need lineTokenizer, so fieldSetFactory should be here.
	private ExcelFieldSetFactory fieldSetFactory = new DefaultExcelFieldSetFactory();
	
	private NumberFormat numberFormat;
	
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public void setNames(Row row){
		String names[] = new String[row.getLastCellNum()];
		for(int i=0;i<names.length;i++){
			names[i] = ObjectUtils.toString(row.getCell(i)).trim();
		}
		fieldSetFactory.setNames(names);
	}
	
	@Override
	public T mapRow(Row row, int rowNumber) throws Exception {
		try{
			return fieldSetMapper.mapFieldSet(fieldSetFactory.create(row));
		}
		catch(Exception e){
			throw e;
		}
	}
	
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(fieldSetMapper, "The FieldSetMapper must be set");
		if(fieldSetFactory instanceof DefaultExcelFieldSetFactory){
			DefaultExcelFieldSetFactory efsf = (DefaultExcelFieldSetFactory)fieldSetFactory;
			if(dateFormat!=null){
				efsf.setDateFormat(dateFormat);
			}
			if(numberFormat!=null){
				efsf.setNumberFormat(numberFormat);
			}
		}
	}
	
	public void setFieldSetMapper(FieldSetMapper<T> fieldSetMapper) {
		this.fieldSetMapper = fieldSetMapper;
	}
	
	public void setDefaultNumberFormat(String numberFormat) {
		this.numberFormat = new DecimalFormat(numberFormat);
	}
	
	public void setDefaultNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}
	
	public void setDefaultDateFormat(String dateFormat) {
		this.dateFormat = new SimpleDateFormat(dateFormat);		
	}
	
	public void setDefaultDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
}
