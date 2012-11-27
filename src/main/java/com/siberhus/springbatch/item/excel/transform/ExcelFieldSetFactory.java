package com.siberhus.springbatch.item.excel.transform;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.item.file.transform.FieldSet;

public interface ExcelFieldSetFactory {
	
	void setNames(String names[]);
	
	FieldSet create(Row row, String[] names);
	
	FieldSet create(Row row);
	
}
