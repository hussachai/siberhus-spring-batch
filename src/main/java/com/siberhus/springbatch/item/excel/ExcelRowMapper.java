package com.siberhus.springbatch.item.excel;

import org.apache.poi.ss.usermodel.Row;

public interface ExcelRowMapper<T> {
	
	void setNames(Row row);
	
	T mapRow(Row row, int rowNumber) throws Exception;
	
}
