package com.siberhus.springbatch.item.excel;

import org.apache.poi.ss.usermodel.Row;

/**
 * Callback interface for handling a line from file. Useful e.g. for header
 * processing.
 * 
 * @author Hussachai Puripunpinyo
 */
public interface ExcelRowCallbackHandler {
	
	void handleRow(Row row);
	
}
