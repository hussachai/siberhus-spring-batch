package com.siberhus.springbatch.item.excel.transform;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

public class DefaultExcelFieldSetFactory implements ExcelFieldSetFactory {
	
	private String names[] = null;
	
	private DateFormat dateFormat;
	
	private NumberFormat numberFormat;
	
	/**
	 * The {@link NumberFormat} to use for parsing numbers. If unset the default
	 * locale will be used.
	 * @param numberFormat the {@link NumberFormat} to use for number parsing
	 */
	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}
	
	/**
	 * The {@link DateFormat} to use for parsing numbers. If unset the default
	 * pattern is ISO standard <code>yyyy/MM/dd</code>.
	 * @param dateFormat the {@link DateFormat} to use for date parsing
	 */
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public void setNames(String names[]){
		this.names = names;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FieldSet create(Row row, String[] names) {
		DefaultFieldSet fieldSet = new DefaultFieldSet(getStringValues(row), names);
		return enhance(fieldSet);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FieldSet create(Row row) {
		if(names!=null){
			return create(row, names);
		}
		DefaultFieldSet fieldSet = new DefaultFieldSet(getStringValues(row));
		return enhance(fieldSet);
	}
	
	private FieldSet enhance(DefaultFieldSet fieldSet) {
		if (dateFormat!=null) {
			fieldSet.setDateFormat(dateFormat);
		}
		if (numberFormat!=null) {
			fieldSet.setDateFormat(dateFormat);
		}	
		return fieldSet;
	}
	
	private String[] getStringValues(Row row){
		String values[] = null;
		if(names!=null){
			values = new String[names.length];
		}else{
			values = new String[row.getLastCellNum()];
		}
		for(int i=0;i<row.getLastCellNum();i++){
			values[i] = getCellValueAsString(row.getCell(i));
		}
		return values;
	}
	
	private String getCellValueAsString(Cell cell) {
		Object value = getCellValue(cell);
		if (value == null) {
			return null;
		} else if (value instanceof Number) {
			return numberFormat.format(((Number) value).doubleValue());
		} else if (value instanceof Date) {
			return dateFormat.format((Date) value);
		} else {
			return value.toString();
		}
	}
	
	private Object getCellValue(Cell cell) {
		if (cell == null)
			return null;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				RichTextString rts = cell.getRichStringCellValue();
				if (rts != null) {
					return rts.getString();
				}
				return null;
			case Cell.CELL_TYPE_NUMERIC:
				String value = cell.toString();
				/*
				 * In POI we cannot know which cell is date or number because both
				 * cells have numeric type To fix this problem we need to call
				 * toString if it's number cell we can parse it but if it's date
				 * cell we cannot parse the value with number parser
				 */
				try {
					numberFormat.parse(value);
					return new BigDecimal(value);
				} catch (Exception e) {
					return cell.getDateCellValue();
				}
			case Cell.CELL_TYPE_BLANK:
				return null;
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue();
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
		}
		return null;
	}
}
